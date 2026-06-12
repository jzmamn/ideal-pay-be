package com.payroll.controller;

import com.payroll.dto.request.BackupRequestDTO;
import com.payroll.dto.request.RestoreRequestDTO;
import com.payroll.dto.response.ApiResponseDTO;
import com.payroll.dto.response.BackupFileDTO;
import com.payroll.dto.response.BackupJobStatusDTO;
import com.payroll.service.BackupRestoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll/backup-restore")
@RequiredArgsConstructor
public class BackupRestoreController {

    private final BackupRestoreService backupRestoreService;

    /** Default (configured) backup directory shown to the admin UI. */
    @GetMapping("/config")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> getConfig() {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Backup configuration fetched successfully",
                Map.of("defaultDirectory", backupRestoreService.getDefaultDirectory())));
    }

    /** List .sql backup files in the given (or default) directory. */
    @GetMapping("/files")
    public ResponseEntity<ApiResponseDTO<List<BackupFileDTO>>> listBackupFiles(
            @RequestParam(value = "path", required = false) String path) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Backup files fetched successfully",
                backupRestoreService.listBackupFiles(path)));
    }

    /** Start a full database backup. Returns the job to poll for progress. */
    @PostMapping("/backup")
    public ResponseEntity<ApiResponseDTO<BackupJobStatusDTO>> startBackup(
            @RequestBody(required = false) BackupRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponseDTO.success(
                        "Backup started",
                        backupRestoreService.startBackup(request)));
    }

    /** Start a database restore from a backup file. Returns the job to poll for progress. */
    @PostMapping("/restore")
    public ResponseEntity<ApiResponseDTO<BackupJobStatusDTO>> startRestore(
            @Valid @RequestBody RestoreRequestDTO request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponseDTO.success(
                        "Restore started",
                        backupRestoreService.startRestore(request)));
    }

    /** Poll the progress / state of a backup or restore job. */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponseDTO<BackupJobStatusDTO>> getJobStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                "Job status fetched successfully",
                backupRestoreService.getJobStatus(jobId)));
    }
}
