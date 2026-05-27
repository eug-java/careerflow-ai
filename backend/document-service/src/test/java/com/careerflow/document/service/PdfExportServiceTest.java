package com.careerflow.document.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfExportServiceTest {

    private final PdfExportService service = new PdfExportService();

    @Test
    void toPdfShouldCreatePdfBytesFromMarkdown() {
        byte[] bytes = service.toPdf("""
                ```markdown
                # John Doe
                - Java developer
                ```
                """);

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, Math.min(bytes.length, 5))).startsWith("%PDF");
    }

    @Test
    void toPdfShouldTreatNullAsEmptyDocument() {
        byte[] bytes = service.toPdf(null);

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, Math.min(bytes.length, 5))).startsWith("%PDF");
    }
}
