package ch.so.arp.rag.chat;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

@Configuration
public class ChatConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatConfiguration.class);

    @Bean
    DocumentSearchService documentSearchService(ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            ObjectProvider<DataSource> dataSourceProvider) {
        NamedParameterJdbcTemplate template = jdbcTemplateProvider.getIfAvailable();
        if (template != null && dataSourceProvider.getIfAvailable() != null && databaseIsReachable(template)) {
            LOGGER.info("Using JDBC-backed hybrid search");
            return new JdbcHybridSearchService(template);
        }
        LOGGER.info("Falling back to mock hybrid search service");
        return new MockHybridSearchService();
    }

    @Bean
    ChatService chatService(Environment environment, ObjectProvider<ChatClient> chatClientProvider) {
        String apiKey = environment.getProperty("spring.ai.openai.api-key");
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (StringUtils.hasText(apiKey) && chatClient != null) {
            LOGGER.info("Using OpenAI chat service");
            return new OpenAiChatService(chatClient);
        }
        LOGGER.info("Falling back to mock chat service");
        return new MockChatService();
    }

    private boolean databaseIsReachable(NamedParameterJdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.getJdbcTemplate().execute("SELECT 1 FROM arp_rag_vp.chunks LIMIT 1");
            return true;
        } catch (Exception ex) {
            LOGGER.warn("Database not reachable, using mock search", ex);
            return false;
        }
    }
}
