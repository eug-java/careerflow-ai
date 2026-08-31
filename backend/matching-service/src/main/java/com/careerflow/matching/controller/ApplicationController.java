package com.careerflow.matching.controller;

import com.careerflow.matching.dto.ApplicationResponse;
import com.careerflow.matching.dto.ApplicationStatus;
import com.careerflow.matching.dto.CreateApplicationRequest;
import com.careerflow.matching.dto.UpdateApplicationRequest;
import com.careerflow.matching.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody CreateApplicationRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public ApplicationResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping
    public List<ApplicationResponse> findAll(@RequestParam(required = false) ApplicationStatus status) {
        return service.findAll(status);
    }

    @PatchMapping("/{id}")
    public ApplicationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationRequest request
    ) {
        return service.update(id, request);
    }
}
