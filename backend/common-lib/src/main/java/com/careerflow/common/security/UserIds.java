package com.careerflow.common.security;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UserIds {

    private UserIds() {
    }

    public static UUID fromUsername(String username) {
        return UUID.nameUUIDFromBytes(("careerflow-user:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
