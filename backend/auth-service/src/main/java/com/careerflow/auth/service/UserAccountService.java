package com.careerflow.auth.service;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


@Service
public class UserAccountService {

    private record UserAccount(String username, String passwordHash, UUID userId) {
    }

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, UserAccount> users;

    public UserAccountService(Environment environment) {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            users = Map.of(
                    "admin", new UserAccount(
                            "admin",
                            passwordEncoder.encode(System.getenv().getOrDefault("CAREERFLOW_ADMIN_PASSWORD", "ChangeMeNow123!")),
                            UUID.fromString("00000000-0000-0000-0000-000000000001")
                    )
            );
        } else {
            users = Map.of(
                    "demo", new UserAccount(
                            "demo",
                            passwordEncoder.encode("demo"),
                            UUID.nameUUIDFromBytes("careerflow-user:demo".getBytes())
                    )
            );
        }
    }

    public UUID authenticate(String username, String password) {
        UserAccount account = users.get(username);
        if (account == null || !passwordEncoder.matches(password, account.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return account.userId();
    }
}
