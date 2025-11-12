package ch.so.arp.rag.chat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Central configuration wiring the chat components together. It exposes toggles
 * that decide whether mocked or real infrastructure components should be used.
 */
@Configuration
@EnableConfigurationProperties(OpenAiClientProperties.class)
public class ChatConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Executor chatExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SseEmitterFactory sseEmitterFactory() {
        return new DefaultSseEmitterFactory();
    }

    @Bean
    @ConditionalOnProperty(name = "rag.chat.mock-openai", havingValue = "true", matchIfMissing = true)
    public LlmClient mockLlmClient() {
        return new MockLlmClient();
    }

    @Bean
    @ConditionalOnProperty(name = "rag.chat.mock-openai", havingValue = "false")
    public LlmClient openAiLlmClient(OpenAiClientProperties properties) {
        return new OpenAiLlmClient(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "rag.chat.mock-vector-store", havingValue = "true", matchIfMissing = true)
    public VectorDatabase mockVectorDatabase() {
        return new MockVectorDatabase();
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingProvider embeddingProvider() {
        return new DeterministicEmbeddingProvider(3072);
    }

    @Bean
    @ConditionalOnMissingBean
    public CrossEncoderReranker crossEncoderReranker() {
        return new TokenOverlapCrossEncoderReranker();
    }

    @Bean
    @ConditionalOnProperty(name = "rag.chat.mock-vector-store", havingValue = "false")
    public VectorDatabase postgresVectorDatabase(JdbcClient jdbcClient, EmbeddingProvider embeddingProvider,
            CrossEncoderReranker reranker) {
        return new PostgresVectorDatabase(jdbcClient, embeddingProvider, reranker);
    }
}
