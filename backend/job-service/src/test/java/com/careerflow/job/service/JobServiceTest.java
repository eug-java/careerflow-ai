package com.careerflow.job.service;

import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.job.dto.CreateJobRequest;
import com.careerflow.job.dto.JobResponse;
import com.careerflow.job.dto.JobSkillRequest;
import com.careerflow.job.entity.JobDescription;
import com.careerflow.job.entity.JobSkill;
import com.careerflow.job.repository.JobRepository;
import com.careerflow.common.test.TestAuthSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository repository;

    @InjectMocks
    private JobService service;

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
        CreateJobRequest request = request(
                "Senior Java Developer",
                List.of(new JobSkillRequest("Java", true), new JobSkillRequest("Kafka", false))
        );

        when(repository.save(any(JobDescription.class))).thenAnswer(invocation -> {
            JobDescription job = invocation.getArgument(0);
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.parse("2026-05-23T10:15:30Z"));
            job.getSkills().forEach(skill -> skill.setId(UUID.randomUUID()));
            return job;
        });

        JobResponse response = service.create(request);

        ArgumentCaptor<JobDescription> captor = ArgumentCaptor.forClass(JobDescription.class);
        verify(repository).save(captor.capture());
        JobDescription saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(saved.getCompanyName()).isEqualTo("CareerFlow");
        assertThat(saved.getSalaryMin()).isEqualByComparingTo("100000.00");
        assertThat(saved.getSalaryMax()).isEqualByComparingTo("150000.00");
        assertThat(saved.getSkills()).hasSize(2);
        assertThat(saved.getSkills()).allSatisfy(skill -> assertThat(skill.getJob()).isSameAs(saved));

        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("Senior Java Developer");
        assertThat(response.skills()).extracting("name").containsExactly("Java", "Kafka");
    }

    @Test
    void createShouldHandleNullSkillsAsEmptyList() {
        CreateJobRequest request = request("Backend Engineer", null);

        when(repository.save(any(JobDescription.class))).thenAnswer(invocation -> {
            JobDescription job = invocation.getArgument(0);
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.now());
            return job;
        });

        JobResponse response = service.create(request);

        assertThat(response.skills()).isEmpty();
    }

    @Test
    void findByIdShouldReturnMappedJob() {
        UUID id = UUID.randomUUID();
        JobDescription job = job(id, "Java Engineer", "Spring", true);
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(job));

        JobResponse response = service.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo("Java Engineer");
        assertThat(response.skills()).hasSize(1);
        assertThat(response.skills().getFirst().name()).isEqualTo("Spring");
    }

    @Test
    void findByIdShouldThrowWhenJobDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found: " + id);
    }

    @Test
    void findAllShouldReturnMappedJobs() {
        JobDescription first = job(UUID.randomUUID(), "Java Developer", "Java", true);
        JobDescription second = job(UUID.randomUUID(), "QA Engineer", "Selenium", false);
        when(repository.findByOwnerId(ownerId)).thenReturn(List.of(first, second));

        List<JobResponse> responses = service.findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(JobResponse::title)
                .containsExactly("Java Developer", "QA Engineer");
    }

    @Test
    void deleteShouldDeleteExistingJob() {
        UUID id = UUID.randomUUID();
        JobDescription job = job(id, "Java Engineer", "Java", true);
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(job));

        service.delete(id);

        verify(repository).delete(job);
    }

    @Test
    void deleteShouldThrowWhenJobDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found: " + id);
        verify(repository, never()).delete(any());
    }

    @Test
    void updateShouldReplaceScalarFieldsAndSkills() {
        UUID id = UUID.randomUUID();
        JobDescription existing = job(id, "Old title", "Old skill", true);
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existing));

        CreateJobRequest request = request(
                "Updated Java Developer",
                List.of(new JobSkillRequest("AWS", true), new JobSkillRequest("Docker", true))
        );

        JobResponse response = service.update(id, request);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo("Updated Java Developer");
        assertThat(response.skills()).extracting("name").containsExactly("AWS", "Docker");
        assertThat(existing.getSkills()).hasSize(2);
        assertThat(existing.getSkills()).allSatisfy(skill -> assertThat(skill.getJob()).isSameAs(existing));
        verify(repository, never()).save(any());
    }

    @Test
    void updateShouldClearSkillsWhenRequestSkillsAreNull() {
        UUID id = UUID.randomUUID();
        JobDescription existing = job(id, "Old title", "Old skill", true);
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existing));

        JobResponse response = service.update(id, request("No skills role", null));

        assertThat(response.skills()).isEmpty();
        assertThat(existing.getSkills()).isEmpty();
    }

    private static CreateJobRequest request(String title, List<JobSkillRequest> skills) {
        return new CreateJobRequest(
                title,
                "CareerFlow",
                "Austin, TX",
                "FULL_TIME",
                new BigDecimal("100000.00"),
                new BigDecimal("150000.00"),
                "USD",
                true,
                "Build backend services",
                skills
        );
    }

    private static JobDescription job(UUID id, String title, String skillName, boolean required) {
        JobDescription job = new JobDescription();
        job.setId(id);
        job.setTitle(title);
        job.setCompanyName("CareerFlow");
        job.setLocation("Austin, TX");
        job.setEmploymentType("FULL_TIME");
        job.setSalaryMin(new BigDecimal("90000.00"));
        job.setSalaryMax(new BigDecimal("140000.00"));
        job.setCurrency("USD");
        job.setRemote(true);
        job.setDescription("Description");
        job.setCreatedAt(Instant.parse("2026-05-23T10:15:30Z"));

        JobSkill skill = new JobSkill();
        skill.setId(UUID.randomUUID());
        skill.setName(skillName);
        skill.setRequired(required);
        skill.setJob(job);
        job.getSkills().add(skill);

        return job;
    }
}
