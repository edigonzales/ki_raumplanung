package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class ChatConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ChatConfiguration.class, InfrastructureConfiguration.class);

    @Test
    void usesMocksByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LlmClient.class);
            assertThat(context).getBean(LlmClient.class).isInstanceOf(MockLlmClient.class);
            assertThat(context).hasSingleBean(VectorDatabase.class);
            assertThat(context).getBean(VectorDatabase.class).isInstanceOf(MockVectorDatabase.class);
        });
    }

    @Test
    void createsRealBeansWhenMocksDisabled() {
        contextRunner
                .withPropertyValues(
                        "rag.chat.mock-openai=false",
                        "rag.chat.mock-vector-store=false",
                        "spring.ai.openai.api-key=test-key",
                        "rag.chat.openai.base-url=https://example.com/v1",
                        "rag.chat.openai.model=gpt-4o")
                .run(context -> {
                    assertThat(context).hasSingleBean(LlmClient.class);
                    assertThat(context).getBean(LlmClient.class).isInstanceOf(OpenAiLlmClient.class);
                    OpenAiClientProperties properties = context.getBean(OpenAiClientProperties.class);
                    assertThat(properties.getApiKey()).isEqualTo("test-key");
                    assertThat(properties.getModel()).isEqualTo("gpt-4o");

                    assertThat(context).hasSingleBean(VectorDatabase.class);
                    assertThat(context).getBean(VectorDatabase.class).isInstanceOf(PostgresVectorDatabase.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class InfrastructureConfiguration {

        @Bean
        JdbcClient jdbcClient(DataSource dataSource) {
            return JdbcClient.create(dataSource);
        }

        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:test;MODE=PostgreSQL");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }
    }
}
