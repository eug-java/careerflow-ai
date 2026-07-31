package com.careerflow.common.security;

import com.careerflow.common.api.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class CurrentUserProvider {

    private CurrentUserProvider() {
    }

    public static CurrentUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ForbiddenException("Authenticated user is required");
        }
        String userId = jwt.getClaimAsString("userId");
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("JWT userId claim is required");
        }
        return new CurrentUser(UUID.fromString(userId), jwt.getSubject());
    }

    public static UUID requireUserId() {
        return requireCurrentUser().userId();
    }
}
