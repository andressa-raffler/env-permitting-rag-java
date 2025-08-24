package com.raffler.env_permitting_rag_java.ingest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Simple ingestion service that allows two strategies when a document is re-ingested:
 * <ul>
 *     <li>TRUNCATE – remove existing {@code document_source} (and cascading
 *     {@code document_chunk}) records for the URI.</li>
 *     <li>VERSIONED – keep previous rows but flag them as {@code obsolete}</li>
 * </ul>
 * The strategy is selected via {@code app.ingestion.strategy} property.
 */
@Service
public class IngestionService {

    public enum Strategy {
        TRUNCATE,
        VERSIONED
    }

    private final JdbcTemplate jdbc;
    private final Strategy strategy;

    public IngestionService(JdbcTemplate jdbc,
                            @Value("${app.ingestion.strategy:truncate}") String strategy) {
        this.jdbc = jdbc;
        this.strategy = Strategy.valueOf(strategy.trim().toUpperCase());
    }

    /**
     * Inserts the document_source row applying the configured strategy for
     * existing data. Actual chunking and embedding generation are handled
     * elsewhere.
     */
    @Transactional
    public void ingest(UUID id, String uri, String title, String version) {
        switch (strategy) {
            case TRUNCATE ->
                    jdbc.update("DELETE FROM document_source WHERE uri = ?", uri);
            case VERSIONED ->
                    jdbc.update("UPDATE document_source SET obsolete = true WHERE uri = ?", uri);
        }
        jdbc.update("INSERT INTO document_source(id, uri, title, version) VALUES (?,?,?,?)",
                id, uri, title, version);
    }
}
