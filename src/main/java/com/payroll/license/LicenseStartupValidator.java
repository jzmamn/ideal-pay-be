package com.payroll.license;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseStartupValidator implements ApplicationRunner {
    private final LicenseService service;
    @Override public void run(ApplicationArguments args) {
        try { log.info("Startup license status: {}", service.validateCurrent().status()); }
        catch (LicenseException ex) { log.warn("Startup license validation failed: {}", ex.getMessage()); }
    }
}
