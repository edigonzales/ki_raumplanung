package ch.so.arp.rag.chat;

import java.util.List;
import java.util.function.Consumer;

/**
 * Deterministic {@link LlmClient} used in tests and local development where the
 * OpenAI API should not be contacted.
 */
class MockLlmClient implements LlmClient {

    private static final List<String> DEFAULT_TOKENS = List.of(
            "This is a mocked response.",
            "Provide an API key to reach the real OpenAI service.");

    @Override
    public void streamChat(String question, List<String> context, Consumer<String> tokenConsumer) {
        tokenConsumer.accept("[mocked answer]");
        DEFAULT_TOKENS.forEach(tokenConsumer);
        if (!context.isEmpty()) {
            tokenConsumer.accept("Relevant context snippets: " + String.join(" | ", context));
        }
        tokenConsumer.accept("Question was: " + question);
        tokenConsumer.accept("Total context snippets: " + context.size());
    }
}
