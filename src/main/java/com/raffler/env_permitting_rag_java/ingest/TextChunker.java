package com.raffler.env_permitting_rag_java.ingest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Splits input text into token-based chunks with configurable size and overlap.
 */
@Component
public class TextChunker {
    private final int sizeTokens;
    private final int overlapTokens;

    public TextChunker(@Value("${poc.chunk.sizeTokens}") int sizeTokens,
                       @Value("${poc.chunk.overlapTokens}") int overlapTokens) {
        this.sizeTokens = sizeTokens;
        this.overlapTokens = overlapTokens;
    }

    /**
     * Split the given text into chunks respecting the configured token size and overlap.
     *
     * @param text input text
     * @return list of chunks
     */
    public List<String> chunk(String text) {
        String[] tokenArr = text.trim().isEmpty() ? new String[0] : text.trim().split("\\s+");
        List<String> tokens = Arrays.asList(tokenArr);
        List<String> chunks = new ArrayList<>();
        if (tokens.isEmpty()) {
            return chunks;
        }

        int step = sizeTokens - overlapTokens;
        for (int start = 0; start < tokens.size(); start += step) {
            int end = Math.min(start + sizeTokens, tokens.size());
            chunks.add(String.join(" ", tokens.subList(start, end)));
            if (end == tokens.size()) {
                break;
            }
        }
        return chunks;
    }
}