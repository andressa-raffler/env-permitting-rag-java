package com.raffler.env_permitting_rag_java.ingest;

import com.raffler.env_permitting_rag_java.ingest.repository.DocumentChunkRepository;
import com.raffler.env_permitting_rag_java.ingest.repository.DocumentSourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IngestionService {
    private final PdfExtractor pdfExtractor;
    private final TextChunker textChunker;
    private final DocumentSourceRepository documentSourceRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public IngestionService(PdfExtractor pdfExtractor,
                            TextChunker textChunker,
                            DocumentSourceRepository documentSourceRepository,
                            DocumentChunkRepository documentChunkRepository) {
        this.pdfExtractor = pdfExtractor;
        this.textChunker = textChunker;
        this.documentSourceRepository = documentSourceRepository;
        this.documentChunkRepository = documentChunkRepository;
    }

    public void ingest(String fileName, String title, String version, List<String> tags) {
        UUID docId = UUID.randomUUID();
        String uri = "file://" + fileName;
        documentSourceRepository.insert(docId, uri, title, version, tags);

        List<PageContent> pages = pdfExtractor.extract(fileName);
        int chunkIndex = 0;
        for (PageContent page : pages) {
            List<String> chunks = textChunker.chunk(page.text());
            for (String chunk : chunks) {
                documentChunkRepository.insert(
                        UUID.randomUUID(),
                        docId,
                        chunkIndex++,
                        chunk,
                        "{}",
                        new float[0]
                );
            }
        }
    }
}