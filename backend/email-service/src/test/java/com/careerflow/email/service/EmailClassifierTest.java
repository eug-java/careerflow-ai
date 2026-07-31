package com.careerflow.email.service;

import com.careerflow.email.dto.EmailCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailClassifierTest {

    private final EmailClassifier classifier = new EmailClassifier();

    @Test
    void classifiesOfferEmail() {
        var result = classifier.classify("Job offer from Acme", "We are pleased to offer you the role");
        assertThat(result.category()).isEqualTo(EmailCategory.OFFER);
    }

    @Test
    void classifiesRejectionEmail() {
        var result = classifier.classify("Application update", "Unfortunately we will not move forward");
        assertThat(result.category()).isEqualTo(EmailCategory.REJECTION);
    }

    @Test
    void classifiesVacancyEmail() {
        var result = classifier.classify("New vacancy", "We are hiring a senior engineer");
        assertThat(result.category()).isEqualTo(EmailCategory.VACANCY);
    }

    @Test
    void classifiesRevisionRequestEmail() {
        var result = classifier.classify("Resume request", "Please send your updated resume");
        assertThat(result.category()).isEqualTo(EmailCategory.REVISION_REQUEST);
    }

    @Test
    void fallsBackToOther() {
        var result = classifier.classify("Hello", "Just checking in");
        assertThat(result.category()).isEqualTo(EmailCategory.OTHER);
    }
}
