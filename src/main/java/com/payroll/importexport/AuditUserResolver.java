package com.payroll.importexport;

import com.payroll.entity.Usr;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.repository.UsrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the audit user: prefers the Spring Security logged-in username,
 * falls back to an explicit userId parameter (legacy callers).
 */
@Component
@RequiredArgsConstructor
public class AuditUserResolver {

    private final UsrRepository usrRepository;

    public Usr resolve(Long fallbackUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return usrRepository.findByUserName(auth.getName())
                    .orElseGet(() -> referenceOrFail(fallbackUserId));
        }
        return referenceOrFail(fallbackUserId);
    }

    private Usr referenceOrFail(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "No authenticated user and no userId provided for audit fields.");
        }
        return usrRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}
