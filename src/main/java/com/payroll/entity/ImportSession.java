package com.payroll.entity;

import com.payroll.enums.ImportEntityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Staged import rows between upload and commit. Expired sessions
 * (TTL 30 minutes) are rejected and purged lazily on access.
 */
@Entity
@Table(name = "import_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportSession {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity", nullable = false, length = 30)
    private ImportEntityType entity;

    @Column(name = "payroll_month", nullable = false, length = 7)
    private String payrollMonth;

    @Column(name = "file_name", length = 255)
    private String fileName;

    /** JSON array of raw parsed rows, keyed by original file header. */
    @Column(name = "rows_json", nullable = false, columnDefinition = "JSON")
    private String rowsJson;

    /** JSON object: expected field → file header. */
    @Column(name = "mapping_json", columnDefinition = "JSON")
    private String mappingJson;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
