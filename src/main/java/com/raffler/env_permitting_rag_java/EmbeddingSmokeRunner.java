package com.raffler.env_permitting_rag_java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
@Profile("smoke")
public class EmbeddingSmokeRunner {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingSmokeRunner.class);

    @Bean
    CommandLineRunner smoke(org.springframework.ai.embedding.EmbeddingModel model) {
        return args -> {
            long t0 = System.nanoTime();
            var vectors = model.embed(java.util.List.of("hello world"));
            long latencyMs = (System.nanoTime() - t0) / 1_000_000;
            System.out.println("SMOKE: dim=" + vectors.get(0).length + " latencyMs=" + latencyMs);
        };
    }


    @Bean
    @ConditionalOnProperty(name = "app.smoke.enabled", havingValue = "true")
    CommandLineRunner smoke(EmbeddingModel embeddingModel,
                            Environment env,
                            @Value("${app.embedding.expected-dim:1536}") int expectedDim) {
        return args -> {
            String apiKey = env.getProperty("spring.ai.openai.api-key", "");
            if (!StringUtils.hasText(apiKey)) {
                log.warn("SMOKE: Skipped (no API key configured). Set OPENAI_API_KEY to run.");
                return;
            }

            long t0 = System.nanoTime();
            List<float[]> outs = embeddingModel.embed(List.of("hello world"));
            long latencyMs = (System.nanoTime() - t0) / 1_000_000;

            float[] vec = outs.get(0);
            int dim = vec.length;

            if (dim != expectedDim) {
                throw new IllegalStateException("SMOKE: embedding dim " + dim + " != expected " + expectedDim);
            }

            log.info("SMOKE: OK — dim={}, latency={} ms", dim, latencyMs);
        };
    }
}
