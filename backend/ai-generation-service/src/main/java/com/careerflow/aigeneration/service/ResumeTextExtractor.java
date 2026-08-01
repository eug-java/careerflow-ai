package com.careerflow.aigeneration.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ResumeTextExtractor {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        try {
            String text = tika.parseToString(file.getInputStream());
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("Could not extract text from uploaded resume");
            }
            return text.trim();
        } catch (IOException | org.apache.tika.exception.TikaException ex) {
            throw new IllegalStateException("Failed to read uploaded resume", ex);
        }
    }
}
