package com.raffler.env_permitting_rag_java;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.Instant;

@SpringBootApplication
public class EmbeddingSmokeRunner {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddingSmokeRunner.class, args);
    }

    @Bean
    CommandLineRunner smoke(EmbeddingModel model) {
        return args -> {
            Instant start = Instant.now();
            float[] vec = model.embed("hello world").clone();
            long ms = Duration.between(start, Instant.now()).toMillis();
            System.out.printf("--------- SMOKE TEST --------- size=%d latency=%dms%n", vec.length, ms);
        };
    }
}
