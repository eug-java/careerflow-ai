package com.careerflow.auth.config;

import com.careerflow.auth.service.UserAccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserSeeder implements ApplicationRunner {

    private final UserAccountService userAccountService;

    public DefaultUserSeeder(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public void run(ApplicationArguments args) {
        userAccountService.seedDefaultUsersIfMissing();
    }
}
