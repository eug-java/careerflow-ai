/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.job.controller;

import com.careerflow.job.dto.CreateJobRequest;
import com.careerflow.job.dto.JobResponse;
import com.careerflow.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping
    public JobResponse create(@RequestBody CreateJobRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<JobResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public JobResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public JobResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateJobRequest request
    ) {
        return service.update(id, request);
    }
}
