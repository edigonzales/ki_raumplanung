package ch.so.arp.rag.chat;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simplified OpenAI client used when the application runs against real
 * infrastructure. The implementation does not reach out to the OpenAI API yet
 * but it incorporates the configured model and retrieved context to produce a
 * deterministic response. This behaviour is intentionally predictable so that
 * tests can assert on the output without accessing external services.
 */
class OpenAiLlmClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final OpenAiClientProperties properties;

    OpenAiLlmClient(OpenAiClientProperties properties) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalArgumentException(
                    "Property 'spring.ai.openai.api-key' must be provided when mocks are disabled");
        }
        this.properties = properties;
    }

    @Override
    public void streamChat(String question, List<String> context, Consumer<String> tokenConsumer) {
        LOGGER.debug("Streaming response with model {} via base URL {}", properties.getModel(), properties.getBaseUrl());
        tokenConsumer.accept("Model: " + properties.getModel());
        tokenConsumer.accept("Question: " + question);
        if (!context.isEmpty()) {
            StringJoiner joiner = new StringJoiner(" | ");
            context.forEach(joiner::add);
            tokenConsumer.accept("Context: " + joiner);
        } else {
            tokenConsumer.accept("Context: none available");
        }
        tokenConsumer.accept("API key used: " + mask(properties.getApiKey()));
        tokenConsumer.accept("Base URL: " + properties.getBaseUrl());
    }

    private String mask(String apiKey) {
        if (apiKey == null || apiKey.length() < 4) {
            return "***";
        }
        return "***" + apiKey.substring(apiKey.length() - 4);
    }
}
