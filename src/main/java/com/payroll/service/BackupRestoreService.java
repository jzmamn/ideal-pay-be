package com.payroll.service;

import com.payroll.dto.request.BackupRequestDTO;
import com.payroll.dto.request.RestoreRequestDTO;
import com.payroll.dto.response.BackupFileDTO;
import com.payroll.dto.response.BackupJobStatusDTO;

import java.util.List;

public interface BackupRestoreService {

    /** Default backup directory (from configuration). */
    String getDefaultDirectory();

    /** List .sql backup files in the given (or default) directory. */
    List<BackupFileDTO> listBackupFiles(String path);

    /** Start an asynchronous full backup; returns the job status with its id. */
    BackupJobStatusDTO startBackup(BackupRequestDTO request);

    /** Start an asynchronous restore from a backup file; returns the job status with its id. */
    BackupJobStatusDTO startRestore(RestoreRequestDTO request);

    /** Current status of a backup/restore job. */
    BackupJobStatusDTO getJobStatus(String jobId);
}
