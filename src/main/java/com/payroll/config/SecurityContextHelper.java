package com.payroll.config;

import com.payroll.entity.Usr;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Convenience helper — call from any service to get the currently authenticated user
 * or their company scope without injecting the full SecurityContext.
 */
@Component
public class SecurityContextHelper {

    public Usr getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usr usr) {
            return usr;
        }
        throw new IllegalStateException("No authenticated user in security context");
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Long getCurrentCompanyId() {
        return getCurrentUser().getCompanyId();
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usr;
    }
}
