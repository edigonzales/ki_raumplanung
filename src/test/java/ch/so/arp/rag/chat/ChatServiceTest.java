package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

class ChatServiceTest {

    @Test
    void streamsTokensAndCompletesSuccessfully() {
        LlmClient llmClient = (question, context, consumer) -> {
            consumer.accept("token-1");
            consumer.accept("token-2");
        };
        VectorDatabase vectorDatabase = (question, limit) -> List.of(
                new RetrievedContext(1L, "doc-1", "Heading", "https://example.invalid/doc-1", "Seite 1", "ctx-1", 0.5d, 0.4d,
                        0.3d, 0.2d),
                new RetrievedContext(2L, "doc-2", "Heading", "https://example.invalid/doc-2", "Seite 2", "ctx-2", 0.1d, 0.2d,
                        0.3d, 0.4d));
        Executor directExecutor = Runnable::run;
        ChatService chatService = new ChatService(llmClient, vectorDatabase, directExecutor);

        List<String> tokens = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicBoolean errored = new AtomicBoolean(false);

        chatService.streamAnswer("What is RAG?", new ChatService.StreamingResponseHandler() {
            @Override
            public void onToken(String token) {
                tokens.add(token);
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable throwable) {
                errored.set(true);
            }
        });

        assertThat(tokens).containsExactly("token-1", "token-2");
        assertThat(completed).isTrue();
        assertThat(errored).isFalse();
    }

    @Test
    void propagatesErrorsFromVectorDatabase() {
        LlmClient llmClient = (question, context, consumer) -> consumer.accept("unused");
        VectorDatabase failingDatabase = (question, limit) -> {
            throw new IllegalStateException("boom");
        };
        Executor directExecutor = Runnable::run;
        ChatService chatService = new ChatService(llmClient, failingDatabase, directExecutor);

        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicBoolean errored = new AtomicBoolean(false);

        chatService.streamAnswer("What is RAG?", new ChatService.StreamingResponseHandler() {
            @Override
            public void onToken(String token) {
                // ignored
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }

            @Override
            public void onError(Throwable throwable) {
                errored.set(true);
                assertThat(throwable).isInstanceOf(IllegalStateException.class);
            }
        });

        assertThat(completed).isFalse();
        assertThat(errored).isTrue();
    }
}
