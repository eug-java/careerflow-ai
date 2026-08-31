package com.careerflow.gateway.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GatewayRateLimiter {

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int maxAttempts, long windowSeconds) {
        Instant now = Instant.now();
        AttemptWindow window = attempts.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAt.isBefore(now)) {
                return new AttemptWindow(1, now.plusSeconds(windowSeconds));
            }
            return new AttemptWindow(current.count + 1, current.expiresAt);
        });
        return window.count <= maxAttempts;
    }

    private record AttemptWindow(int count, Instant expiresAt) {
    }
}
