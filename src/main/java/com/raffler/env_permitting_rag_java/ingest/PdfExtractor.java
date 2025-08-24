package com.raffler.env_permitting_rag_java.ingest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PdfExtractor {
    private static final Logger log = LoggerFactory.getLogger(PdfExtractor.class);
    private final Path docsDir;

    public PdfExtractor() {
        this(Path.of("data", "docs"));
    }

    public PdfExtractor(Path docsDir) {
        this.docsDir = docsDir;
    }

    public List<PageContent> extract(String fileName) {
        Path pdfPath = docsDir.resolve(fileName);
        if (!Files.exists(pdfPath)) {
            log.warn("PDF not found: {}", pdfPath.toAbsolutePath());
            return List.of();
        }

        List<PageContent> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document);
                pages.add(new PageContent(i, text));
            }
        } catch (IOException e) {
            log.warn("Failed to read PDF {}: {}", pdfPath.toAbsolutePath(), e.getMessage());
        }
        return pages;
    }
}