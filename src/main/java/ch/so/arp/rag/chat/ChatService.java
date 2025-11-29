package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Coordinates the retrieval of context from the vector database and delegates
 * the answer generation to the large language model integration.
 */
@Service
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private static final int DEFAULT_CONTEXT_LIMIT = 8;

    private final LlmClient llmClient;
    private final VectorDatabase vectorDatabase;
    private final Executor chatExecutor;

    public ChatService(LlmClient llmClient, VectorDatabase vectorDatabase, Executor chatExecutor) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
        this.vectorDatabase = Objects.requireNonNull(vectorDatabase, "vectorDatabase");
        this.chatExecutor = Objects.requireNonNull(chatExecutor, "chatExecutor");
    }

    public void streamAnswer(String question, StreamingResponseHandler handler) {
        Objects.requireNonNull(handler, "handler");
        chatExecutor.execute(() -> {
            try {
                List<RetrievedContext> retrievedContexts = vectorDatabase.findRelevantContext(question,
                        DEFAULT_CONTEXT_LIMIT);
                List<String> context = retrievedContexts.stream().map(RetrievedContext::formatForPrompt).toList();
                llmClient.streamChat(question, context, handler::onToken);
                handler.onComplete();
            } catch (Exception ex) {
                LOGGER.error("Failed to produce response for question '{}': {}", question, ex.getMessage(), ex);
                handler.onError(ex);
            }
        });
    }

    /**
     * Callback API allowing to react to the streaming behaviour of the service.
     */
    public interface StreamingResponseHandler {

        void onToken(String token);

        void onComplete();

        void onError(Throwable throwable);
    }
}
