/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.matching.controller;

import com.careerflow.matching.dto.CreateMatchRequest;
import com.careerflow.matching.dto.MatchResultResponse;
import com.careerflow.matching.service.MatchingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchingController {

    private final MatchingService service;

    public MatchingController(MatchingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResultResponse calculate(@Valid @RequestBody CreateMatchRequest request) {
        return service.calculate(request);
    }

    @GetMapping("/{id}")
    public MatchResultResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping
    public List<MatchResultResponse> findAll(
            @RequestParam(required = false) UUID profileId,
            @RequestParam(required = false) UUID jobId
    ) {
        return service.findAll(profileId, jobId);
    }
}
