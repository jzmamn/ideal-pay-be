package com.payroll.service.impl;

import com.payroll.dto.request.BackupRequestDTO;
import com.payroll.dto.request.RestoreRequestDTO;
import com.payroll.dto.response.BackupFileDTO;
import com.payroll.dto.response.BackupJobStatusDTO;
import com.payroll.exception.BackupRestoreException;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.service.BackupRestoreService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Full database backup / restore implemented purely over JDBC — no external
 * mysqldump / mysql binaries required.
 *
 * Backup produces a single self-contained .sql file containing
 * {@code DROP TABLE IF EXISTS} + {@code CREATE TABLE} + batched
 * {@code INSERT} statements for every base table, followed by view
 * definitions. Restore streams the file back, splitting statements with a
 * quote/comment-aware parser, so it also accepts standard mysqldump files.
 *
 * Jobs run on a single-threaded executor: only one backup or restore may be
 * in flight at a time. Progress is exposed for polling via
 * {@link #getJobStatus(String)}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupRestoreServiceImpl implements BackupRestoreService {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[\\w][\\w.\\- ]*\\.sql$");
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int INSERT_BATCH_ROWS = 500;

    private final DataSource dataSource;

    @Value("${app.backup.directory:./backups}")
    private String defaultDirectory;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "backup-restore-worker");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final AtomicReference<Job> activeJob = new AtomicReference<>();

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    public String getDefaultDirectory() {
        return Paths.get(defaultDirectory).toAbsolutePath().normalize().toString();
    }

    @Override
    public List<BackupFileDTO> listBackupFiles(String path) {
        Path dir = resolveDirectory(path, false);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".sql"))
                    .filter(Files::isRegularFile)
                    .map(this::toFileDto)
                    .sorted(Comparator.comparing(BackupFileDTO::getCreatedAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new BackupRestoreException("Unable to list backup directory: " + dir, e);
        }
    }

    @Override
    public BackupJobStatusDTO startBackup(BackupRequestDTO request) {
        Path dir = resolveDirectory(request != null ? request.getPath() : null, true);
        Path file = dir.resolve("ideal_pay_full_backup_" + LocalDateTime.now().format(FILE_STAMP) + ".sql");

        Job job = newJob("BACKUP", file);
        submit(job, () -> runBackup(job, file));
        return toDto(job);
    }

    @Override
    public BackupJobStatusDTO startRestore(RestoreRequestDTO request) {
        String fileName = request.getFileName();
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Invalid backup file name: " + fileName);
        }
        Path dir = resolveDirectory(request.getPath(), false);
        Path file = dir.resolve(fileName).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Backup file not found: " + fileName);
        }

        Job job = newJob("RESTORE", file);
        submit(job, () -> runRestore(job, file));
        return toDto(job);
    }

    @Override
    public BackupJobStatusDTO getJobStatus(String jobId) {
        Job job = jobs.get(jobId);
        if (job == null) {
            throw new ResourceNotFoundException("Backup/restore job not found: " + jobId);
        }
        return toDto(job);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    // ── Job orchestration ─────────────────────────────────────────────────────

    private synchronized Job newJob(String type, Path file) {
        Job running = activeJob.get();
        if (running != null && "RUNNING".equals(running.state)) {
            throw new IllegalStateException(
                    "A " + running.type.toLowerCase(Locale.ROOT) + " job is already running. "
                            + "Wait for it to finish before starting another.");
        }
        Job job = new Job(UUID.randomUUID().toString(), type, file.toAbsolutePath().toString());
        jobs.put(job.id, job);
        activeJob.set(job);
        return job;
    }

    private void submit(Job job, ThrowingRunnable work) {
        executor.submit(() -> {
            try {
                work.run();
                job.progress = 100;
                job.currentStep = "Done";
                job.state = "COMPLETED";
            } catch (Exception e) {
                log.error("{} job {} failed", job.type, job.id, e);
                job.error = rootMessage(e);
                job.state = "FAILED";
            } finally {
                job.finishedAt = LocalDateTime.now();
            }
        });
    }

    // ── Backup ────────────────────────────────────────────────────────────────

    private void runBackup(Job job, Path file) throws SQLException, IOException {
        try (Connection con = dataSource.getConnection()) {
            String database = con.getCatalog();
            List<String> tables = listObjects(con, "BASE TABLE");
            List<String> views = listObjects(con, "VIEW");

            Map<String, Long> rowEstimates = estimateRows(con, tables);
            long totalWork = rowEstimates.values().stream().mapToLong(v -> Math.max(v, 1)).sum();
            long doneWork = 0;

            job.totalTables = tables.size() + views.size();

            Path tmp = file.resolveSibling(file.getFileName() + ".part");
            try (BufferedWriter out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                writeHeader(out, database);

                for (String table : tables) {
                    job.currentStep = "Backing up table " + table;
                    dumpTable(con, out, table);
                    job.tablesDone++;
                    doneWork += Math.max(rowEstimates.getOrDefault(table, 0L), 1);
                    job.progress = (int) Math.min(99, doneWork * 100 / Math.max(totalWork, 1));
                }

                for (String view : views) {
                    job.currentStep = "Backing up view " + view;
                    dumpView(con, out, view);
                    job.tablesDone++;
                }

                writeFooter(out);
            } catch (SQLException | IOException | RuntimeException e) {
                Files.deleteIfExists(tmp);
                throw e;
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writeHeader(BufferedWriter out, String database) throws IOException {
        out.write("-- Ideal Pay full database backup\n");
        out.write("-- Database: " + database + "\n");
        out.write("-- Generated: " + LocalDateTime.now() + "\n\n");
        out.write("SET NAMES utf8mb4;\n");
        out.write("SET FOREIGN_KEY_CHECKS = 0;\n");
        out.write("SET UNIQUE_CHECKS = 0;\n");
        out.write("SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';\n\n");
    }

    private void writeFooter(BufferedWriter out) throws IOException {
        out.write("SET FOREIGN_KEY_CHECKS = 1;\n");
        out.write("SET UNIQUE_CHECKS = 1;\n");
    }

    private List<String> listObjects(Connection con, String type) throws SQLException {
        String sql = """
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_type = ?
                ORDER BY table_name""";
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }
        return names;
    }

    private Map<String, Long> estimateRows(Connection con, List<String> tables) throws SQLException {
        Map<String, Long> estimates = new HashMap<>();
        String sql = """
                SELECT table_name, table_rows FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'""";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                estimates.put(rs.getString(1), rs.getLong(2));
            }
        }
        tables.forEach(t -> estimates.putIfAbsent(t, 0L));
        return estimates;
    }

    private void dumpTable(Connection con, BufferedWriter out, String table) throws SQLException, IOException {
        String quoted = quoteIdentifier(table);

        out.write("--\n-- Table: " + table + "\n--\n");
        out.write("DROP TABLE IF EXISTS " + quoted + ";\n");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + quoted)) {
            if (rs.next()) {
                out.write(rs.getString(2) + ";\n\n");
            }
        }

        try (Statement st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            st.setFetchSize(Integer.MIN_VALUE); // MySQL driver streaming mode
            try (ResultSet rs = st.executeQuery("SELECT * FROM " + quoted)) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                StringBuilder prefix = new StringBuilder("INSERT INTO ").append(quoted).append(" (");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) prefix.append(", ");
                    prefix.append(quoteIdentifier(meta.getColumnName(i)));
                }
                prefix.append(") VALUES\n");

                int rowsInBatch = 0;
                StringBuilder batch = new StringBuilder();
                while (rs.next()) {
                    if (rowsInBatch > 0) batch.append(",\n");
                    batch.append('(');
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) batch.append(", ");
                        appendValue(batch, rs, meta, i);
                    }
                    batch.append(')');
                    rowsInBatch++;

                    if (rowsInBatch >= INSERT_BATCH_ROWS) {
                        out.write(prefix.toString());
                        out.write(batch.toString());
                        out.write(";\n");
                        batch.setLength(0);
                        rowsInBatch = 0;
                    }
                }
                if (rowsInBatch > 0) {
                    out.write(prefix.toString());
                    out.write(batch.toString());
                    out.write(";\n");
                }
                out.write("\n");
            }
        }
    }

    private void dumpView(Connection con, BufferedWriter out, String view) throws SQLException, IOException {
        String quoted = quoteIdentifier(view);
        out.write("--\n-- View: " + view + "\n--\n");
        out.write("DROP VIEW IF EXISTS " + quoted + ";\n");
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + quoted)) {
            if (rs.next()) {
                out.write(rs.getString(2) + ";\n\n");
            }
        }
    }

    private void appendValue(StringBuilder sb, ResultSet rs, ResultSetMetaData meta, int col)
            throws SQLException {
        int type = meta.getColumnType(col);
        switch (type) {
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> {
                byte[] bytes = rs.getBytes(col);
                if (rs.wasNull()) {
                    sb.append("NULL");
                } else if (bytes.length == 0) {
                    sb.append("''");
                } else {
                    sb.append("0x");
                    for (byte b : bytes) {
                        sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                          .append(Character.forDigit(b & 0xF, 16));
                    }
                }
            }
            case Types.BIT, Types.BOOLEAN -> {
                boolean v = rs.getBoolean(col);
                sb.append(rs.wasNull() ? "NULL" : (v ? "1" : "0"));
            }
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT,
                 Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL -> {
                String v = rs.getString(col);
                sb.append(rs.wasNull() ? "NULL" : v);
            }
            default -> {
                String v = rs.getString(col);
                if (rs.wasNull()) {
                    sb.append("NULL");
                } else {
                    appendQuotedString(sb, v);
                }
            }
        }
    }

    private void appendQuotedString(StringBuilder sb, String value) {
        sb.append('\'');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\'' -> sb.append("\\'");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\0' -> sb.append("\\0");
                case '\u001A' -> sb.append("\\Z");
                default -> sb.append(c);
            }
        }
        sb.append('\'');
    }

    private String quoteIdentifier(String name) {
        return "`" + name.replace("`", "``") + "`";
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    private void runRestore(Job job, Path file) throws SQLException, IOException {
        long totalBytes = Math.max(Files.size(file), 1);
        job.currentStep = "Restoring from " + file.getFileName();

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            st.execute("SET UNIQUE_CHECKS = 0");

            SqlStatementSplitter splitter = new SqlStatementSplitter(reader);
            long bytesDone = 0;
            String sql;
            int statements = 0;
            while ((sql = splitter.next()) != null) {
                bytesDone += sql.length() + 1;
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) continue;
                st.execute(trimmed);
                statements++;
                if (statements % 10 == 0) {
                    job.currentStep = "Executed " + statements + " statements";
                }
                job.progress = (int) Math.min(99, bytesDone * 100 / totalBytes);
            }

            st.execute("SET FOREIGN_KEY_CHECKS = 1");
            st.execute("SET UNIQUE_CHECKS = 1");
        }
    }

    /**
     * Splits an SQL script into individual statements, honouring single/double
     * quoted strings (with backslash escapes), backtick identifiers, line
     * comments ({@code --}, {@code #}) and block comments. Comment-only
     * content between statements is dropped.
     */
    static final class SqlStatementSplitter {

        private final BufferedReader reader;

        SqlStatementSplitter(BufferedReader reader) {
            this.reader = reader;
        }

        String next() throws IOException {
            StringBuilder sb = new StringBuilder();
            int ci;
            char quote = 0;          // active quote char: ' " `
            boolean escaped = false; // previous char was a backslash inside quotes
            boolean lineComment = false;
            boolean blockComment = false;
            char prev = 0;

            while ((ci = reader.read()) != -1) {
                char c = (char) ci;

                if (lineComment) {
                    if (c == '\n') lineComment = false;
                    prev = c;
                    continue;
                }
                if (blockComment) {
                    if (prev == '*' && c == '/') blockComment = false;
                    prev = c;
                    continue;
                }
                if (quote != 0) {
                    sb.append(c);
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\' && quote != '`') {
                        escaped = true;
                    } else if (c == quote) {
                        quote = 0;
                    }
                    prev = c;
                    continue;
                }

                // Not inside quote or comment
                if (c == '\'' || c == '"' || c == '`') {
                    quote = c;
                    sb.append(c);
                } else if (c == '#') {
                    lineComment = true;
                } else if (c == '-' && peekEquals('-')) {
                    lineComment = true;
                } else if (c == '/' && peekEquals('*')) {
                    blockComment = true;
                    prev = 0;
                    continue;
                } else if (c == ';') {
                    if (!sb.toString().trim().isEmpty()) {
                        return sb.toString();
                    }
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
                prev = c;
            }
            String rest = sb.toString().trim();
            return rest.isEmpty() ? null : rest;
        }

        private boolean peekEquals(char expected) throws IOException {
            reader.mark(1);
            int next = reader.read();
            if (next == expected) {
                return true;
            }
            reader.reset();
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Path resolveDirectory(String override, boolean create) {
        String raw = (override == null || override.isBlank()) ? defaultDirectory : override.trim();
        Path dir = Paths.get(raw).toAbsolutePath().normalize();
        if (create) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new BackupRestoreException("Unable to create backup directory: " + dir, e);
            }
        }
        return dir;
    }

    private BackupFileDTO toFileDto(Path p) {
        try {
            FileTime time = Files.getLastModifiedTime(p);
            return BackupFileDTO.builder()
                    .fileName(p.getFileName().toString())
                    .sizeBytes(Files.size(p))
                    .createdAt(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()))
                    .build();
        } catch (IOException e) {
            throw new BackupRestoreException("Unable to read backup file metadata: " + p, e);
        }
    }

    private BackupJobStatusDTO toDto(Job job) {
        return BackupJobStatusDTO.builder()
                .jobId(job.id)
                .type(job.type)
                .state(job.state)
                .progress(job.progress)
                .currentStep(job.currentStep)
                .tablesDone(job.tablesDone)
                .totalTables(job.totalTables)
                .filePath(job.filePath)
                .error(job.error)
                .startedAt(job.startedAt)
                .finishedAt(job.finishedAt)
                .build();
    }

    private String rootMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        return (msg == null || msg.isBlank()) ? root.getClass().getSimpleName() : msg;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Mutable job state shared between worker thread and polling requests. */
    private static final class Job {
        final String id;
        final String type;
        final String filePath;
        final LocalDateTime startedAt = LocalDateTime.now();

        volatile String state = "RUNNING";
        volatile int progress = 0;
        volatile String currentStep = "Starting";
        volatile int tablesDone = 0;
        volatile int totalTables = 0;
        volatile String error;
        volatile LocalDateTime finishedAt;

        Job(String id, String type, String filePath) {
            this.id = id;
            this.type = type;
            this.filePath = filePath;
        }
    }
}
