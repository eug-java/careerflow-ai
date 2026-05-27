/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.controller;

import com.careerflow.profile.dto.CandidateProfileResponse;
import com.careerflow.profile.dto.CreateCandidateProfileRequest;
import com.careerflow.profile.service.CandidateProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Candidate Profiles", description = "Candidate profile management API")
public class CandidateProfileController {

    private final CandidateProfileService service;

    public CandidateProfileController(CandidateProfileService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateProfileResponse create(@Valid @RequestBody CreateCandidateProfileRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<CandidateProfileResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CandidateProfileResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public CandidateProfileResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCandidateProfileRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
