package com.careerflow.common.security;

import java.util.UUID;

public record CurrentUser(UUID userId, String username) {
}
