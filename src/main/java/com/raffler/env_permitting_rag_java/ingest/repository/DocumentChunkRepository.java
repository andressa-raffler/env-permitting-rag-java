package com.raffler.env_permitting_rag_java.ingest.repository;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class DocumentChunkRepository {
    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(UUID id,
                      UUID docId,
                      int chunkIndex,
                      String text,
                      String metadataJson,
                      float[] embedding) {
        String vectorLiteral = toVectorLiteral(embedding);
        String sql = ("""
                INSERT INTO document_chunk(id, doc_id, chunk_index, text, metadata, embedding)
                VALUES (?, ?, ?, ?, ?::jsonb, %s)
                """).formatted(vectorLiteral);
        return jdbcTemplate.update(sql, id, docId, chunkIndex, text, metadataJson);
    }

    private static String toVectorLiteral(float[] embedding) {
        String joined = IntStream.range(0, embedding.length)
                .mapToObj(i -> Float.toString(embedding[i]))
                .collect(Collectors.joining(","));
        return "('" + "[" + joined + "]')::vector";
    }
}