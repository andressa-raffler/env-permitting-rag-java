package com.raffler.env_permitting_rag_java;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("integrationTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractIntegrationTests {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16")
                    .withDatabaseName("licgpt")
                    .withUsername("licgpt")
                    .withPassword("licgpt");

    @BeforeAll
    static void ensureRunning() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        String jdbcUrl = POSTGRES.getJdbcUrl();
        jdbcUrl = jdbcUrl.contains("?") ? jdbcUrl + "&currentSchema=licgpt" : jdbcUrl + "?currentSchema=licgpt";

        String finalJdbcUrl = jdbcUrl;
        registry.add("spring.datasource.url", () -> finalJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.liquibase.change-log", () -> "classpath:/db/db.changelog-master.yaml");
        registry.add("spring.liquibase.default-schema", () -> "licgpt");
        registry.add("spring.liquibase.liquibase-schema", () -> "public");
        registry.add("spring.liquibase.parameters.schemaName", () -> "licgpt");
        registry.add("spring.liquibase.parameters.embeddingDims", () -> "1536");
        registry.add("spring.liquibase.parameters.ivfflatLists", () -> "100");
    }
}
