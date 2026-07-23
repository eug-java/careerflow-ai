package com.careerflow.matching.service;

import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.matching.client.JobClient;
import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileClient;
import com.careerflow.matching.client.ProfileResponse;
import com.careerflow.matching.dto.CreateMatchRequest;
import com.careerflow.matching.dto.MatchResultResponse;
import com.careerflow.matching.entity.JobMatchResult;
import com.careerflow.matching.repository.JobMatchResultRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private ProfileClient profileClient;

    @Mock
    private JobClient jobClient;

    @Mock
    private MatchingScoreCalculator scoreCalculator;

    @Mock
    private JobMatchResultRepository repository;

    @InjectMocks
    private MatchingService service;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        ownerId = TestAuthSupport.authenticateTestUser();
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void calculateFetchesProfileAndJobCalculatesScorePersistsResultAndReturnsResponse() throws Exception {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        CreateMatchRequest request = new CreateMatchRequest(profileId, jobId);
        ProfileResponse profile = new ProfileResponse(profileId, "Jane", "Developer", "jane@example.com", null, "Austin, TX", null, List.of());
        JobResponse job = new JobResponse(jobId, "Java Developer", "Company", "Austin, TX", "FULL_TIME", null, null, "USD", false, "Description", List.of());
        MatchingScoreCalculator.MatchScore score = new MatchingScoreCalculator.MatchScore(
                new BigDecimal("88.50"),
                new BigDecimal("90.00"),
                new BigDecimal("100"),
                new BigDecimal("55"),
                "Good match"
        );
        UUID resultId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-23T12:00:00Z");

        when(profileClient.getProfile(profileId)).thenReturn(profile);
        when(jobClient.getJob(jobId)).thenReturn(job);
        when(scoreCalculator.calculate(profile, job)).thenReturn(score);
        when(repository.save(any(JobMatchResult.class))).thenAnswer(invocation -> {
            JobMatchResult result = invocation.getArgument(0);
            setField(result, "id", resultId);
            setField(result, "createdAt", createdAt);
            return result;
        });

        MatchResultResponse response = service.calculate(request);

        assertThat(response.id()).isEqualTo(resultId);
        assertThat(response.profileId()).isEqualTo(profileId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.totalScore()).isEqualByComparingTo("88.50");
        assertThat(response.skillsScore()).isEqualByComparingTo("90.00");
        assertThat(response.locationScore()).isEqualByComparingTo("100");
        assertThat(response.salaryScore()).isEqualByComparingTo("55");
        assertThat(response.explanation()).isEqualTo("Good match");
        assertThat(response.createdAt()).isEqualTo(createdAt);

        ArgumentCaptor<JobMatchResult> captor = ArgumentCaptor.forClass(JobMatchResult.class);
        verify(repository).save(captor.capture());
        JobMatchResult saved = captor.getValue();
        assertThat(saved.getProfileId()).isEqualTo(profileId);
        assertThat(saved.getJobId()).isEqualTo(jobId);
        assertThat(saved.getTotalScore()).isEqualByComparingTo("88.50");
        assertThat(saved.getExplanation()).isEqualTo("Good match");
        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void findByIdReturnsExistingMatch() throws Exception {
        UUID id = UUID.randomUUID();
        JobMatchResult result = result(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(result));

        MatchResultResponse response = service.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.totalScore()).isEqualByComparingTo("75.00");
    }

    @Test
    void findByIdThrowsWhenMatchDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Match result not found")
                .hasMessageContaining(id.toString());
    }

    @Test
    void findAllFiltersByProfileIdWhenProfileIdIsProvided() throws Exception {
        UUID profileId = UUID.randomUUID();
        JobMatchResult result = result(UUID.randomUUID(), profileId, UUID.randomUUID());
        when(repository.findByOwnerIdAndProfileId(ownerId, profileId)).thenReturn(List.of(result));

        List<MatchResultResponse> responses = service.findAll(profileId, UUID.randomUUID());

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().profileId()).isEqualTo(profileId);
        verify(repository).findByOwnerIdAndProfileId(ownerId, profileId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findAllFiltersByJobIdWhenOnlyJobIdIsProvided() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobMatchResult result = result(UUID.randomUUID(), UUID.randomUUID(), jobId);
        when(repository.findByOwnerIdAndJobId(ownerId, jobId)).thenReturn(List.of(result));

        List<MatchResultResponse> responses = service.findAll(null, jobId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().jobId()).isEqualTo(jobId);
        verify(repository).findByOwnerIdAndJobId(ownerId, jobId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findAllReturnsAllMatchesWhenNoFiltersProvided() throws Exception {
        JobMatchResult first = result(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        JobMatchResult second = result(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.findByOwnerId(ownerId)).thenReturn(List.of(first, second));

        List<MatchResultResponse> responses = service.findAll(null, null);

        assertThat(responses).hasSize(2);
    }

    private static JobMatchResult result(UUID id, UUID profileId, UUID jobId) throws Exception {
        JobMatchResult result = new JobMatchResult();
        setField(result, "id", id);
        setField(result, "createdAt", Instant.parse("2026-05-23T12:00:00Z"));
        result.setProfileId(profileId);
        result.setJobId(jobId);
        result.setTotalScore(new BigDecimal("75.00"));
        result.setSkillsScore(new BigDecimal("80.00"));
        result.setLocationScore(new BigDecimal("70.00"));
        result.setSalaryScore(new BigDecimal("50.00"));
        result.setExplanation("Explanation");
        return result;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
