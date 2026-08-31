package com.careerflow.auth.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String key) {
        Instant now = Instant.now();
        AttemptWindow window = attempts.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt.isBefore(now)) {
                return new AttemptWindow(1, now.plusSeconds(WINDOW_SECONDS));
            }
            return new AttemptWindow(current.count + 1, current.expiresAt);
        });

        if (window.count > MAX_ATTEMPTS) {
            throw new IllegalStateException("Too many login attempts. Try again later.");
        }
    }

    private record AttemptWindow(int count, Instant expiresAt) {
    }
}
