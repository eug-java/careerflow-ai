/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class DocxExportService {

    public byte[] toDocx(String markdownText) {
        String text = removeMarkdownCodeFence(markdownText);

        try (
                XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            String[] lines = text.split("\\R");

            for (String line : lines) {
                addLine(document, line);
            }

            document.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to export document to DOCX", e);
        }
    }

    private void addLine(XWPFDocument document, String line) {
        if (line == null || line.isBlank()) {
            document.createParagraph();
            return;
        }

        String trimmed = line.trim();

        if (trimmed.startsWith("# ")) {
            addHeading(document, trimmed.substring(2), 1);
            return;
        }

        if (trimmed.startsWith("## ")) {
            addHeading(document, trimmed.substring(3), 2);
            return;
        }

        if (trimmed.startsWith("### ")) {
            addHeading(document, trimmed.substring(4), 3);
            return;
        }

        if (trimmed.startsWith("- ")) {
            addBullet(document, trimmed.substring(2));
            return;
        }

        addParagraph(document, trimmed);
    }

    private void addHeading(XWPFDocument document, String text, int level) {
        XWPFParagraph paragraph = document.createParagraph();

        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setText(cleanInlineMarkdown(text));

        if (level == 1) {
            run.setFontSize(20);
        } else if (level == 2) {
            run.setFontSize(14);
        } else {
            run.setFontSize(12);
        }
    }

    private void addBullet(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setIndentationLeft(360);
        paragraph.setIndentationHanging(180);

        XWPFRun run = paragraph.createRun();
        run.setText("• " + cleanInlineMarkdown(text));
        run.setFontSize(11);
    }

    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();

        XWPFRun run = paragraph.createRun();
        run.setText(cleanInlineMarkdown(text));
        run.setFontSize(11);
    }

    private String cleanInlineMarkdown(String value) {
        return value
                .replace("**", "")
                .replace("__", "")
                .replace("*", "")
                .replace("`", "");
    }

    private String removeMarkdownCodeFence(String value) {
        if (value == null) {
            return "";
        }

        String text = value.trim();

        if (text.startsWith("```markdown")) {
            text = text.substring("```markdown".length()).trim();
        } else if (text.startsWith("```")) {
            text = text.substring("```".length()).trim();
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3).trim();
        }

        return text;
    }
}
