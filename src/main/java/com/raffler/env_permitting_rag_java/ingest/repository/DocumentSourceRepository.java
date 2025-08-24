package com.raffler.env_permitting_rag_java.ingest.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class DocumentSourceRepository {
    private final JdbcTemplate jdbcTemplate;

    public DocumentSourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(UUID id, String uri, String title, String version, List<String> tags) {
        String tagsLiteral = toTagsLiteral(tags);
        String sql = ("""
                INSERT INTO document_source(id, uri, title, version, tags)
                VALUES (?, ?, ?, ?, %s)
                """).formatted(tagsLiteral);
        return jdbcTemplate.update(sql, id, uri, title, version);
    }

    private static String toTagsLiteral(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "ARRAY[]::TEXT[]";
        }
        String joined = tags.stream()
                .map(t -> "'" + t.replace("'", "''") + "'")
                .collect(Collectors.joining(","));
        return "ARRAY[" + joined + "]::TEXT[]";
    }
}