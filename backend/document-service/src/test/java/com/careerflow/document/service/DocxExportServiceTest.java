package com.careerflow.document.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DocxExportServiceTest {

    private final DocxExportService service = new DocxExportService();

    @Test
    void toDocxShouldCreateReadableDocxAndCleanMarkdownSyntax() throws Exception {
        String markdown = """
                ```markdown
                # John Doe
                ## Experience
                - **Java** developer
                Regular `text`
                ```
                """;

        byte[] bytes = service.toDocx(markdown);

        assertThat(bytes).isNotEmpty();
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String text = document.getParagraphs()
                    .stream()
                    .map(paragraph -> paragraph.getText())
                    .reduce("", (left, right) -> left + "\n" + right);

            assertThat(text).contains("John Doe");
            assertThat(text).contains("Experience");
            assertThat(text).contains("• Java developer");
            assertThat(text).contains("Regular text");
            assertThat(text).doesNotContain("**");
            assertThat(text).doesNotContain("`");
        }
    }

    @Test
    void toDocxShouldTreatNullAsEmptyDocument() {
        byte[] bytes = service.toDocx(null);

        assertThat(bytes).isNotEmpty();
    }
}
