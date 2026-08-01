package com.careerflow.aigeneration.controller;

import com.careerflow.aigeneration.dto.AiAccountResponse;
import com.careerflow.aigeneration.dto.AiConnectionTestResponse;
import com.careerflow.aigeneration.dto.UpsertAiAccountRequest;
import com.careerflow.aigeneration.service.AiCredentialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiAccountController {

    private final AiCredentialService aiCredentialService;

    public AiAccountController(AiCredentialService aiCredentialService) {
        this.aiCredentialService = aiCredentialService;
    }

    @GetMapping("/account")
    public AiAccountResponse getAccount() {
        return aiCredentialService.getAccount();
    }

    @PutMapping("/account")
    public AiAccountResponse upsertAccount(@Valid @RequestBody UpsertAiAccountRequest request) {
        return aiCredentialService.upsertAccount(request);
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount() {
        aiCredentialService.deleteAccount();
    }

    @PostMapping("/account/test")
    public AiConnectionTestResponse testConnection(@Valid @RequestBody UpsertAiAccountRequest request) {
        return aiCredentialService.testConnection(request);
    }
}
