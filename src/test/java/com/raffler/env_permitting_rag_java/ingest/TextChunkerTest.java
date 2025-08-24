package com.raffler.env_permitting_rag_java.ingest;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextChunkerTest {

    @Test
    void chunkBoundariesRespectSizeAndOverlap() {
        TextChunker chunker = new TextChunker(4, 1);
        List<String> chunks = chunker.chunk("a b c d e f g h i j");
        assertThat(chunks).containsExactly(
                "a b c d",
                "d e f g",
                "g h i j"
        );
    }

    @Test
    void chunksMaintainConfiguredOverlap() {
        TextChunker chunker = new TextChunker(5, 2);
        List<String> chunks = chunker.chunk("w1 w2 w3 w4 w5 w6 w7 w8 w9 w10 w11");
        for (int i = 0; i < chunks.size() - 1; i++) {
            List<String> current = Arrays.asList(chunks.get(i).split(" "));
            List<String> next = Arrays.asList(chunks.get(i + 1).split(" "));
            assertThat(next.subList(0, 2))
                    .isEqualTo(current.subList(current.size() - 2, current.size()));
        }
    }
}