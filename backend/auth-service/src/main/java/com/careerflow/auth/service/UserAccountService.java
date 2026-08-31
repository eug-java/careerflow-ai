package com.careerflow.auth.service;

import com.careerflow.auth.dto.RegisterRequest;
import com.careerflow.auth.entity.UserAccountEntity;
import com.careerflow.auth.repository.UserAccountRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final boolean prodProfile;

    public UserAccountService(UserAccountRepository userAccountRepository, Environment environment) {
        this.userAccountRepository = userAccountRepository;
        this.prodProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    public UUID authenticate(String username, String password) {
        UserAccountEntity account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return account.getUserId();
    }

    @Transactional
    public UUID register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username already taken");
        }

        UUID userId = UUID.nameUUIDFromBytes(("careerflow-user:" + request.username()).getBytes());
        UserAccountEntity account = new UserAccountEntity(
                userId,
                request.username(),
                passwordEncoder.encode(request.password())
        );
        userAccountRepository.save(account);
        return userId;
    }

    @Transactional
    public void seedDefaultUsersIfMissing() {
        if (prodProfile) {
            seedIfMissing(
                    "admin",
                    System.getenv().getOrDefault("CAREERFLOW_ADMIN_PASSWORD", "ChangeMeNow123!"),
                    UUID.fromString("00000000-0000-0000-0000-000000000001")
            );
            return;
        }

        seedIfMissing(
                "demo",
                "demo",
                UUID.nameUUIDFromBytes("careerflow-user:demo".getBytes())
        );
    }

    private void seedIfMissing(String username, String password, UUID userId) {
        if (userAccountRepository.existsByUsername(username)) {
            return;
        }
        userAccountRepository.save(new UserAccountEntity(
                userId,
                username,
                passwordEncoder.encode(password)
        ));
    }
}
