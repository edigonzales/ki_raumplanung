package ch.so.arp.rag.chat;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.StringUtils;

@Configuration
public class ChatConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatConfiguration.class);

    @Bean
    DocumentSearchService documentSearchService(ObjectProvider<JdbcClient> jdbcClientProvider,
            ObjectProvider<DataSource> dataSourceProvider, QueryEmbeddingService embeddingService) {
        JdbcClient jdbcClient = jdbcClientProvider.getIfAvailable();
        if (jdbcClient != null && dataSourceProvider.getIfAvailable() != null && databaseIsReachable(jdbcClient)) {
            LOGGER.info("Using JDBC-backed hybrid search");
            return new JdbcHybridSearchService(jdbcClient, embeddingService);
        }
        LOGGER.info("Falling back to mock hybrid search service");
        return new MockHybridSearchService();
    }

    @Bean
    ChatService chatService(Environment environment, ObjectProvider<ChatClient> chatClientProvider,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider, DocumentSearchService documentSearchService,
            PromptFactory promptFactory, TaskContextStore contextStore) {
        boolean openAiEnabled = environment.getProperty("spring.ai.openai.enabled", Boolean.class, true)
                && environment.getProperty("spring.ai.openai.chat.enabled", Boolean.class, true);
        String apiKey = environment.getProperty("spring.ai.openai.api-key");
        if (!openAiEnabled || !StringUtils.hasText(apiKey)) {
            LOGGER.info("Falling back to mock chat service");
            return new MockChatService(contextStore);
        }

        ChatClient chatClient = chatClientProvider.getIfAvailable();

        if (chatClient == null) {
            ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
            if (builder != null) {
                chatClient = builder.build();
            }
        }
        if (chatClient != null) {
            LOGGER.info("Using OpenAI chat service");
            return new OpenAiChatService(chatClient, documentSearchService, promptFactory, contextStore);
        }
        LOGGER.info("Falling back to mock chat service");
        return new MockChatService(contextStore);
    }

    @Bean
    QueryEmbeddingService queryEmbeddingService(Environment environment,
            ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        boolean openAiEnabled = environment.getProperty("spring.ai.openai.enabled", Boolean.class, true)
                && environment.getProperty("spring.ai.openai.embedding.enabled", Boolean.class, true);
        String apiKey = environment.getProperty("spring.ai.openai.api-key");

        if (!openAiEnabled || !StringUtils.hasText(apiKey)) {
            LOGGER.info("Falling back to mock embedding service");
            return new MockQueryEmbeddingService();
        }

        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel != null) {
            LOGGER.info("Using OpenAI embedding service");
            return new OpenAiQueryEmbeddingService(embeddingModel);
        }

        LOGGER.info("Embedding model unavailable, using mock embedding service");
        return new MockQueryEmbeddingService();
    }

    private boolean databaseIsReachable(JdbcClient jdbcClient) {
        try {
            jdbcClient.sql("SELECT 1 FROM arp_rag_vp.chunks LIMIT 1").query(Integer.class).single();
            return true;
        } catch (Exception ex) {
            LOGGER.warn("Database not reachable, using mock search", ex);
            return false;
        }
    }
}
