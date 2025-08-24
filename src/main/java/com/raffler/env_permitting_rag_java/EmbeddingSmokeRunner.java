package com.raffler.env_permitting_rag_java;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EmbeddingSmokeRunner {

    @Bean
    CommandLineRunner smoke(OpenAiEmbeddingModel model) {
        return args -> {
            long start = System.currentTimeMillis();
            float[] resp = model.embed("hello world");
            long latency = System.currentTimeMillis() - start;
            List<Float> vec = new ArrayList<>();
            vec.add(resp[0]);
            System.out.println("Embedding vector size: " + vec.size());
            System.out.println("Latency: " + latency + " ms");
        };
    }

}