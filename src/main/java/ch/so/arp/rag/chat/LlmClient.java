package ch.so.arp.rag.chat;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstraction over the language model integration. Implementations can either
 * invoke the real OpenAI API or return predictable responses for testing.
 */
public interface LlmClient {

    /**
     * Stream answer tokens for the given question and context.
     *
     * @param question the user question
     * @param context relevant context retrieved from the vector store
     * @param tokenConsumer callback invoked for every generated token
     */
    void streamChat(String question, List<String> context, Consumer<String> tokenConsumer);
}
