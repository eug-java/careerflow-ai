/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.profile.service;

import com.careerflow.profile.dto.*;
import com.careerflow.profile.entity.CandidateExperience;
import com.careerflow.profile.entity.CandidateProfile;
import com.careerflow.profile.entity.CandidateSkill;
import com.careerflow.profile.repository.CandidateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.careerflow.profile.exception.ProfileNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository repository;

    public CandidateProfileService(CandidateProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CandidateProfileResponse create(CreateCandidateProfileRequest request) {
        CandidateProfile profile = new CandidateProfile();

        applyProfileFields(profile, request);
        replaceSkills(profile, request.skills());
        replaceExperiences(profile, request.experiences());

        return toResponse(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<CandidateProfileResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse findById(UUID id) {
        CandidateProfile profile = findEntityById(id);
        return toResponse(profile);
    }

//    @Transactional
//    public CandidateProfileResponse update(UUID id, CreateCandidateProfileRequest request) {
//        CandidateProfile profile = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
//
//        profile.setFullName(request.fullName());
//        profile.setProfessionalTitle(request.professionalTitle());
//        profile.setEmail(request.email());
//        profile.setPhone(request.phone());
//        profile.setLocation(request.location());
//        profile.setSummary(request.summary());
//
//        return toResponse(profile);
//    }

    @Transactional
    public CandidateProfileResponse update(UUID id, CreateCandidateProfileRequest request) {
        CandidateProfile profile = findEntityById(id);

        applyProfileFields(profile, request);
        replaceSkills(profile, request.skills());
        replaceExperiences(profile, request.experiences());

        return toResponse(repository.save(profile));
    }

    @Transactional
    public void delete(UUID id) {
        CandidateProfile profile = findEntityById(id);
        repository.delete(profile);
    }
//    @Transactional
//    public void delete(UUID id) {
//        Profile profile = repository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
//
//        repository.delete(profile);

//    }

    private CandidateProfile findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }

    private void applyProfileFields(CandidateProfile profile, CreateCandidateProfileRequest request) {
        profile.setFullName(request.fullName());
        profile.setProfessionalTitle(request.professionalTitle());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());
        profile.setLocation(request.location());
        profile.setSummary(request.summary());
    }

    private void replaceSkills(CandidateProfile profile, List<SkillRequest> skillRequests) {
        profile.getSkills().clear();

        if (skillRequests == null) {
            return;
        }

        for (SkillRequest skillRequest : skillRequests) {
            CandidateSkill skill = new CandidateSkill();
            skill.setName(skillRequest.name());
            skill.setCategory(skillRequest.category());
            skill.setYearsOfExperience(skillRequest.yearsOfExperience());
            skill.setProfile(profile);

            profile.getSkills().add(skill);
        }
    }

    private void replaceExperiences(CandidateProfile profile, List<ExperienceRequest> experienceRequests) {
        profile.getExperiences().clear();

        if (experienceRequests == null) {
            return;
        }

        for (ExperienceRequest experienceRequest : experienceRequests) {
            CandidateExperience experience = new CandidateExperience();
            experience.setCompanyName(experienceRequest.companyName());
            experience.setPositionTitle(experienceRequest.positionTitle());
            experience.setLocation(experienceRequest.location());
            experience.setStartDate(experienceRequest.startDate());
            experience.setEndDate(experienceRequest.endDate());
            experience.setCurrentPosition(experienceRequest.currentPosition());
            experience.setDescription(experienceRequest.description());
            experience.setProfile(profile);

            profile.getExperiences().add(experience);
        }
    }

    private CandidateProfileResponse toResponse(CandidateProfile profile) {
        return new CandidateProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getProfessionalTitle(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getLocation(),
                profile.getSummary(),
                toSkillResponses(profile.getSkills()),
                toExperienceResponses(profile.getExperiences()),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private List<SkillResponse> toSkillResponses(List<CandidateSkill> skills) {
        return skills.stream()
                .map(skill -> new SkillResponse(
                        skill.getId(),
                        skill.getName(),
                        skill.getCategory(),
                        skill.getYearsOfExperience()
                ))
                .toList();
    }

    private List<ExperienceResponse> toExperienceResponses(List<CandidateExperience> experiences) {
        return experiences.stream()
                .map(experience -> new ExperienceResponse(
                        experience.getId(),
                        experience.getCompanyName(),
                        experience.getPositionTitle(),
                        experience.getLocation(),
                        experience.getStartDate(),
                        experience.getEndDate(),
                        experience.isCurrentPosition(),
                        experience.getDescription()
                ))
                .toList();
    }
}
