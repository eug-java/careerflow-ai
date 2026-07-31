package com.careerflow.email.controller;

import com.careerflow.email.dto.*;
import com.careerflow.email.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/account")
    public EmailAccountResponse getAccount() {
        return emailService.getAccount();
    }

    @PutMapping("/account")
    public EmailAccountResponse upsertAccount(@Valid @RequestBody UpsertEmailAccountRequest request) {
        return emailService.upsertAccount(request);
    }

    @DeleteMapping("/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount() {
        emailService.deleteAccount();
    }

    @PostMapping("/account/test")
    public ConnectionTestResponse testConnection(@Valid @RequestBody UpsertEmailAccountRequest request) {
        return emailService.testConnection(request);
    }

    @PostMapping("/sync")
    public SyncEmailResponse syncInbox() {
        return emailService.syncInbox();
    }

    @GetMapping("/summary")
    public EmailSummaryResponse summary() {
        return emailService.summary();
    }

    @GetMapping("/messages")
    public List<InboxMessageResponse> listMessages(@RequestParam(required = false) EmailCategory category) {
        return emailService.listMessages(category);
    }

    @GetMapping("/messages/{id}")
    public InboxMessageResponse getMessage(@PathVariable UUID id) {
        return emailService.getMessage(id);
    }

    @PostMapping("/messages/{id}/reply")
    public InboxMessageResponse reply(@PathVariable UUID id, @Valid @RequestBody ReplyToEmailRequest request) {
        return emailService.reply(id, request);
    }
}
