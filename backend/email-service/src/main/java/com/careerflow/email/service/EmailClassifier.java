package com.careerflow.email.service;

import com.careerflow.email.dto.EmailCategory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class EmailClassifier {

    private static final Map<EmailCategory, String[]> KEYWORDS = Map.of(
            EmailCategory.OFFER, new String[] {
                    "offer letter", "pleased to offer", "job offer", "congratulations",
                    "we are delighted", "оффер", "предлагаем вам", "поздравляем"
            },
            EmailCategory.REJECTION, new String[] {
                    "unfortunately", "not moving forward", "regret to inform", "reject",
                    "other candidates", "к сожалению", "отказ", "не готовы", "отклон"
            },
            EmailCategory.VACANCY, new String[] {
                    "job opening", "vacancy", "we are hiring", "open position", "apply now",
                    "recruiter", "talent acquisition", "вакансия", "ищем", "приглашаем"
            },
            EmailCategory.REVISION_REQUEST, new String[] {
                    "updated resume", "updated cv", "cover letter", "revised resume",
                    "send your resume", "attach resume", "исправлен", "резюме", "сопроводительное"
            }
    );

    public ClassificationResult classify(String subject, String body) {
        String combined = ((subject == null ? "" : subject) + " " + (body == null ? "" : body))
                .toLowerCase(Locale.ROOT);

        for (Map.Entry<EmailCategory, String[]> entry : KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (combined.contains(keyword)) {
                    return new ClassificationResult(entry.getKey(), "Matched keyword: " + keyword);
                }
            }
        }

        return new ClassificationResult(EmailCategory.OTHER, "No recruiter keyword matched");
    }

    public record ClassificationResult(EmailCategory category, String reason) {
    }
}
