package com.raffler.env_permitting_rag_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration checks for Liquibase migrations (schema, tables, indexes)
 * and a minimal insert to validate vector column usage.
 * <p>
 * Notes on style:
 * - Centralized constants for schema & embedding dims
 * - Small helper methods for existence checks
 * - Java text blocks with a newline right after opening quotes (required by the language)
 */
public class DatabaseMigrationsIT extends AbstractIntegrationTests {

    private static final String SCHEMA = "licgpt";
    private static final int EMBEDDING_DIMS = 1536;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void schema_and_extensions_exist() {
        boolean hasSchema = Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)",
                Boolean.class, SCHEMA));
        assertThat(hasSchema).isTrue();

        List<String> extensions = jdbc.queryForList(
                "SELECT extname FROM pg_extension WHERE extname IN ('vector','pgcrypto')",
                String.class);
        assertThat(extensions).contains("vector"); // pgcrypto may or may not be present
    }

    @Test
    void tables_exist() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = ?",
                String.class, SCHEMA);
        assertThat(tables).contains("document_source", "document_chunk");
    }

    @Test
    void indexes_exist() {
        assertThat(indexExists("idx_document_chunk_doc")).isTrue();
        assertThat(indexExists("idx_document_chunk_meta_uf")).isTrue();
        assertThat(indexExists("idx_document_chunk_embedding")).isTrue();
    }

    @Test
    void can_insert_document_source_and_chunk() {
        UUID docId = UUID.randomUUID();

        int rows = jdbc.update(
                """
                        INSERT INTO %s.document_source(id, uri, title, version, tags)
                        VALUES (?, ?, ?, ?, ARRAY[]::TEXT[])
                        """.formatted(SCHEMA),
                docId, "file://inttest/sample.pdf#p=1", "IT Rule", "v1"
        );
        assertThat(rows).isEqualTo(1);

        String zeroVector = zeroVectorLiteral(EMBEDDING_DIMS);
        int rows2 = jdbc.update(
                """
                        INSERT INTO %s.document_chunk(id, doc_id, chunk_index, text, metadata, embedding)
                        VALUES (?, ?, 0, ?, '{"section":"IT","uf":"SP"}'::jsonb, %s)
                        """.formatted(SCHEMA, zeroVector),
                UUID.randomUUID(), docId, "Dummy text for IT"
        );
        assertThat(rows2).isEqualTo(1);
    }

    // ---------- helpers ----------

    private boolean indexExists(String indexName) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes WHERE schemaname = ? AND indexname = ?)",
                Boolean.class, SCHEMA, indexName);
        return Boolean.TRUE.equals(exists);
    }

    private static String zeroVectorLiteral(int dims) {
        // Builds a Postgres literal like: ('[' || array_to_string(ARRAY(SELECT 0.0 FROM generate_series(1, dims)), ',') || ']')::vector
        return "('[' || array_to_string(ARRAY(SELECT 0.0 FROM generate_series(1, " + dims + ")), ',') || ']')::vector";
    }
}
