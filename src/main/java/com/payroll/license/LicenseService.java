package com.payroll.license;

import com.payroll.entity.Company;
import com.payroll.entity.Usr;
import com.payroll.repository.CompanyRepository;
import com.payroll.repository.EmployeeRepository;
import com.payroll.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LicenseService {
    private final SoftwareLicenseRepository licenseRepository;
    private final LicenseAuditLogRepository auditRepository;
    private final LicenseCryptoService cryptoService;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UsrRepository usrRepository;
    private final Clock clock = Clock.systemDefaultZone();

    @Transactional
    public LicenseValidationResult importLicense(LicenseImportRequest request) {
        LicensePayload payload = cryptoService.verifyAndRead(request.licenseContent());
        validatePayloadIntegrity(payload);
        Company company = companyRepository.findAll().stream().filter(c -> c.getId() != null && c.getId() > 0).findFirst()
                .orElseThrow(() -> new LicenseException(LicenseStatus.INVALID, "Company configuration was not found"));
        if (!payload.customerCode().equalsIgnoreCase(company.getCode()) || !payload.customerName().equalsIgnoreCase(company.getName())) {
            throw new LicenseException(LicenseStatus.INVALID, "License customer does not match the configured company");
        }
        Usr user = usrRepository.findById(request.installedBy())
                .orElseThrow(() -> new LicenseException(LicenseStatus.INVALID, "Installing user was not found"));
        licenseRepository.clearCurrent();
        LicenseValidationResult result = evaluate(payload);
        SoftwareLicense entity = licenseRepository.findAll().stream()
                .filter(l -> l.getLicenseId().equals(payload.licenseId())).findFirst().orElseGet(SoftwareLicense::new);
        entity.setLicenseId(payload.licenseId());
        entity.setCustomerCode(payload.customerCode());
        entity.setCustomerName(payload.customerName());
        entity.setPlan(payload.plan());
        entity.setEmployeeLimit(payload.employeeLimit());
        entity.setValidFrom(payload.validFrom());
        entity.setValidTill(payload.validTill());
        entity.setMaintenanceAvailable(payload.maintenanceAvailable());
        entity.setLicenseStatus(result.status());
        entity.setRawLicenseKey(request.licenseContent().trim());
        entity.setCurrent(true);
        entity.setInstalledBy(user);
        entity.setInstalledAt(LocalDateTime.now(clock));
        if (entity.getCreatedBy() == null) entity.setCreatedBy(user);
        entity.setModifiedBy(user);
        licenseRepository.save(entity);
        audit(payload.licenseId(), "IMPORT", result.status().name(), result.message(), user);
        return result;
    }

    @Transactional
    public LicenseValidationResult validateCurrent() {
        SoftwareLicense installed = currentEntity();
        try {
            LicensePayload payload = cryptoService.verifyAndRead(installed.getRawLicenseKey());
            validatePayloadIntegrity(payload);
            ensureProjectionMatches(installed, payload);
            LicenseValidationResult result = evaluate(payload);
            installed.setLicenseStatus(result.status());
            licenseRepository.save(installed);
            audit(payload.licenseId(), "VALIDATE", result.status().name(), result.message(), systemUser());
            return result;
        } catch (LicenseException ex) {
            installed.setLicenseStatus(ex.getStatus());
            licenseRepository.save(installed);
            audit(installed.getLicenseId(), "VALIDATE", ex.getStatus().name(), ex.getMessage(), systemUser());
            throw ex;
        }
    }

    public LicenseValidationResult current() { return validateCurrent(); }

    public void requirePayrollAllowed() {
        LicenseValidationResult result = validateCurrent();
        if (result.status() != LicenseStatus.ACTIVE && result.status() != LicenseStatus.EXPIRING_SOON) {
            throw new LicenseException(result.status(), result.message());
        }
    }

    public void requireEmployeeSlot() {
        requirePayrollAllowed();
        long count = realActiveEmployeeCount();
        LicenseValidationResult result = validateCurrent();
        if (count >= result.employeeLimit()) {
            throw new LicenseException(LicenseStatus.EMPLOYEE_LIMIT_EXCEEDED,
                    "Employee limit of " + result.employeeLimit() + " has been reached");
        }
    }

    public void requireMaintenance() {
        LicenseValidationResult result = validateCurrent();
        if (!result.maintenanceAvailable()) {
            throw new LicenseException(LicenseStatus.INVALID, "This license does not include maintenance access");
        }
    }

    private LicenseValidationResult evaluate(LicensePayload payload) {
        LocalDate today = LocalDate.now(clock);
        long employees = realActiveEmployeeCount();
        LicenseStatus status;
        String message;
        if (today.isBefore(payload.validFrom())) { status = LicenseStatus.NOT_ACTIVE; message = "License is not active yet"; }
        else if (today.isAfter(payload.validTill())) { status = LicenseStatus.EXPIRED; message = "License expired on " + payload.validTill(); }
        else if (employees > payload.employeeLimit()) { status = LicenseStatus.EMPLOYEE_LIMIT_EXCEEDED; message = "Active employee count exceeds the licensed limit"; }
        else if (!payload.validTill().isAfter(today.plusDays(30))) { status = LicenseStatus.EXPIRING_SOON; message = "License expires within 30 days"; }
        else { status = LicenseStatus.ACTIVE; message = "License is active"; }
        return new LicenseValidationResult(status, message, payload.licenseId(), payload.plan(), payload.employeeLimit(),
                employees, payload.validFrom(), payload.validTill(), payload.maintenanceAvailable());
    }

    private void validatePayloadIntegrity(LicensePayload payload) {
        if (payload.plan() == null || payload.employeeLimit() != payload.plan().getEmployeeLimit())
            throw new LicenseException(LicenseStatus.INVALID, "License plan and employee limit do not match");
        if (payload.validFrom() == null || payload.validTill() == null || payload.validTill().isBefore(payload.validFrom()))
            throw new LicenseException(LicenseStatus.INVALID, "License validity dates are invalid");
    }

    private void ensureProjectionMatches(SoftwareLicense e, LicensePayload p) {
        if (!e.getLicenseId().equals(p.licenseId()) || e.getPlan() != p.plan()
                || e.getEmployeeLimit() != p.employeeLimit() || !e.getValidFrom().equals(p.validFrom())
                || !e.getValidTill().equals(p.validTill()) || e.getMaintenanceAvailable() != p.maintenanceAvailable())
            throw new LicenseException(LicenseStatus.INVALID, "Stored license values do not match the signed license");
    }

    private long realActiveEmployeeCount() { return Math.max(0, employeeRepository.countByIsActive(true) - 1); }
    private SoftwareLicense currentEntity() { return licenseRepository.findFirstByCurrentTrue()
            .orElseThrow(() -> new LicenseException(LicenseStatus.INVALID, "No license is installed")); }
    private Usr systemUser() { return usrRepository.findById(-1L).orElseGet(() -> currentEntity().getInstalledBy()); }
    private void audit(String id, String action, String status, String message, Usr user) {
        auditRepository.save(LicenseAuditLog.builder().licenseId(id).action(action).status(status).message(message).performedBy(user).build());
    }
}

