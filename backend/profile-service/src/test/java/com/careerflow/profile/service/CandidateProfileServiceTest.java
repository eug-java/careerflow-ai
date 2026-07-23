
package com.careerflow.profile.service;

import com.careerflow.profile.dto.CreateCandidateProfileRequest;
import com.careerflow.profile.dto.ExperienceRequest;
import com.careerflow.profile.dto.SkillRequest;
import com.careerflow.profile.entity.CandidateExperience;
import com.careerflow.profile.entity.CandidateProfile;
import com.careerflow.profile.entity.CandidateSkill;
import com.careerflow.profile.exception.ProfileNotFoundException;
import com.careerflow.profile.repository.CandidateProfileRepository;
import com.careerflow.common.test.TestAuthSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServiceTest {

    @Mock
    private CandidateProfileRepository repository;

    @InjectMocks
    private CandidateProfileService service;

    private UUID ownerId;

    @BeforeEach
    void setUpAuth() {
        ownerId = TestAuthSupport.authenticateTestUser();
    }

    @AfterEach
    void tearDownAuth() {
        TestAuthSupport.clear();
    }

    @Test
    void createShouldMapRequestToEntityAndReturnResponse() {
        var request = fullRequest();
        when(repository.save(any(CandidateProfile.class))).thenAnswer(invocation -> {
            CandidateProfile profile = invocation.getArgument(0);
            invokeLifecycle(profile, "prePersist");
            profile.getSkills().forEach(skill -> invokeLifecycle(skill, "prePersist"));
            profile.getExperiences().forEach(experience -> invokeLifecycle(experience, "prePersist"));
            return profile;
        });

        var response = service.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.fullName()).isEqualTo("Evgenii Buianov");
        assertThat(response.professionalTitle()).isEqualTo("Java Backend Developer");
        assertThat(response.email()).isEqualTo("eug.java.dev@gmail.com");
        assertThat(response.skills()).hasSize(2);
        assertThat(response.skills().getFirst().name()).isEqualTo("Java");
        assertThat(response.skills().getFirst().yearsOfExperience()).isEqualByComparingTo("6.0");
        assertThat(response.experiences()).hasSize(1);
        assertThat(response.experiences().getFirst().companyName()).isEqualTo("Bank");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        ArgumentCaptor<CandidateProfile> captor = ArgumentCaptor.forClass(CandidateProfile.class);
        verify(repository).save(captor.capture());
        CandidateProfile savedProfile = captor.getValue();

        assertThat(savedProfile.getSkills())
                .allSatisfy(skill -> assertThat(skill.getProfile()).isSameAs(savedProfile));
        assertThat(savedProfile.getExperiences())
                .allSatisfy(experience -> assertThat(experience.getProfile()).isSameAs(savedProfile));
    }

    @Test
    void createShouldHandleNullNestedCollections() {
        var request = new CreateCandidateProfileRequest(
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                null,
                null,
                null,
                null,
                null
        );
        when(repository.save(any(CandidateProfile.class))).thenAnswer(invocation -> {
            CandidateProfile profile = invocation.getArgument(0);
            invokeLifecycle(profile, "prePersist");
            return profile;
        });

        var response = service.create(request);

        assertThat(response.skills()).isEmpty();
        assertThat(response.experiences()).isEmpty();
    }

    @Test
    void findAllShouldReturnMappedProfiles() {
        CandidateProfile profile = persistedProfile(ownerId);
        when(repository.findByOwnerId(ownerId)).thenReturn(List.of(profile));

        var responses = service.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(profile.getId());
        assertThat(responses.getFirst().fullName()).isEqualTo("Evgenii Buianov");
        verify(repository).findByOwnerId(ownerId);
    }

    @Test
    void findByIdShouldReturnProfileWhenExists() {
        CandidateProfile profile = persistedProfile(ownerId);
        when(repository.findByIdAndOwnerId(profile.getId(), ownerId)).thenReturn(Optional.of(profile));

        var response = service.findById(profile.getId());

        assertThat(response.id()).isEqualTo(profile.getId());
        assertThat(response.fullName()).isEqualTo("Evgenii Buianov");
    }

    @Test
    void findByIdShouldThrowWhenProfileDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ProfileNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateShouldReplaceProfileFieldsSkillsAndExperiences() {
        CandidateProfile existing = persistedProfile(ownerId);
        CandidateSkill oldSkill = new CandidateSkill();
        oldSkill.setName("Old skill");
        oldSkill.setProfile(existing);
        existing.getSkills().add(oldSkill);

        CandidateExperience oldExperience = new CandidateExperience();
        oldExperience.setCompanyName("Old company");
        oldExperience.setPositionTitle("Old position");
        oldExperience.setProfile(existing);
        existing.getExperiences().add(oldExperience);

        when(repository.findByIdAndOwnerId(existing.getId(), ownerId)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(existing.getId(), fullRequest());

        assertThat(response.fullName()).isEqualTo("Evgenii Buianov");
        assertThat(existing.getSkills()).extracting(CandidateSkill::getName)
                .containsExactly("Java", "Spring Boot");
        assertThat(existing.getExperiences()).extracting(CandidateExperience::getCompanyName)
                .containsExactly("Bank");
        assertThat(existing.getSkills()).allSatisfy(skill -> assertThat(skill.getProfile()).isSameAs(existing));
        assertThat(existing.getExperiences()).allSatisfy(experience -> assertThat(experience.getProfile()).isSameAs(existing));
        verify(repository).save(existing);
    }

    @Test
    void deleteShouldRemoveProfileWhenExists() {
        CandidateProfile profile = persistedProfile(ownerId);
        when(repository.findByIdAndOwnerId(profile.getId(), ownerId)).thenReturn(Optional.of(profile));

        service.delete(profile.getId());

        verify(repository).delete(profile);
    }

    private static CreateCandidateProfileRequest fullRequest() {
        return new CreateCandidateProfileRequest(
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                "+1 512 555 0100",
                "Austin, TX",
                "Java backend engineer with Spring Boot experience.",
                List.of(
                        new SkillRequest("Java", "Backend", new BigDecimal("6.0")),
                        new SkillRequest("Spring Boot", "Framework", new BigDecimal("5.0"))
                ),
                List.of(
                        new ExperienceRequest(
                                "Bank",
                                "Senior Java Developer",
                                "Austin, TX",
                                LocalDate.of(2021, 1, 1),
                                null,
                                true,
                                "Built microservices."
                        )
                )
        );
    }

    private static CandidateProfile persistedProfile(UUID ownerId) {
        CandidateProfile profile = new CandidateProfile();
        profile.setFullName("Evgenii Buianov");
        profile.setProfessionalTitle("Java Backend Developer");
        profile.setEmail("eug.java.dev@gmail.com");
        profile.setPhone("+1 512 555 0100");
        profile.setLocation("Austin, TX");
        profile.setSummary("Java backend engineer.");
        profile.setOwnerId(ownerId);
        invokeLifecycle(profile, "prePersist");
        return profile;
    }

    private static void invokeLifecycle(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to invoke " + methodName, exception);
        }
    }
}
