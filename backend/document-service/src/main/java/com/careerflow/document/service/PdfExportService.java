/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.document.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService {

    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public byte[] toPdf(String markdownText) {
        String cleanedMarkdown = removeMarkdownCodeFence(markdownText);
        String bodyHtml = htmlRenderer.render(markdownParser.parse(cleanedMarkdown));

        String html = """
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: Letter;
                            margin: 0.55in;
                        }

                        body {
                            font-family: Arial, sans-serif;
                            font-size: 10.5px;
                            line-height: 1.35;
                            color: #111827;
                        }

                        h1 {
                            font-size: 22px;
                            margin: 0 0 4px 0;
                            color: #111827;
                        }

                        h2 {
                            font-size: 13px;
                            margin: 14px 0 6px 0;
                            padding-bottom: 3px;
                            border-bottom: 1px solid #d1d5db;
                            color: #111827;
                        }

                        h3 {
                            font-size: 11.5px;
                            margin: 10px 0 4px 0;
                        }

                        p {
                            margin: 4px 0;
                        }

                        ul {
                            margin: 4px 0 8px 18px;
                            padding: 0;
                        }

                        li {
                            margin: 2px 0;
                        }

                        strong {
                            font-weight: 700;
                        }

                        a {
                            color: #111827;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    %s
                </body>
                </html>
                """.formatted(bodyHtml);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export document to PDF", e);
        }
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
