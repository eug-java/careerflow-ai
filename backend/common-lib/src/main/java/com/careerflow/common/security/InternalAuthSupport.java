package com.careerflow.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class InternalAuthSupport {

    private InternalAuthSupport() {
    }

    public static boolean isInternalCall() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_INTERNAL".equals(a.getAuthority()));
    }
}
