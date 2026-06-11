package com.payroll.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize} permission checks (e.g. on
 * PayrollPeriodController) when {@code app.security.method-security.enabled=true}.
 *
 * Disabled by default: the application currently runs with a permit-all
 * security chain and no authentication provider, so enabling method security
 * unconditionally would reject every request. Flip the property once
 * JWT/session authentication is in place.
 */
@Configuration
@ConditionalOnProperty(name = "app.security.method-security.enabled", havingValue = "true")
@EnableMethodSecurity
public class MethodSecurityConfig {
}
