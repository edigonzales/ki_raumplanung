package ch.so.arp.rag.chat;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Mock LLM interaction that streams a synthetic answer as SSE events.
 */
public class MockChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockChatService.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public SseEmitter streamSummary(SummaryStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        executor.execute(() -> sendMockResponse(emitter, request));
        return emitter;
    }

    private void sendMockResponse(SseEmitter emitter, SummaryStreamRequest request) {
        List<String> rows = request.selection().stream()
                .map(id -> "• Abschnitt " + id)
                .toList();

        String preamble = "Zusammenfassung für \"" + request.prompt() + "\"";
        String details = String.join("\n", rows);
        String closing = "Mock-Antwort basierend auf den ausgewählten Abschnitten.";

        try {
            emitter.send(SseEmitter.event().name("message").data(preamble));
            pause();
            emitter.send(SseEmitter.event().name("message").data(details));
            pause();
            emitter.send(SseEmitter.event().name("message").data(closing));
            emitter.complete();
        } catch (IOException e) {
            LOGGER.warn("Failed to send SSE response", e);
            emitter.completeWithError(e);
        }
    }

    private void pause() {
        try {
            Thread.sleep(Duration.ofMillis(300));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
