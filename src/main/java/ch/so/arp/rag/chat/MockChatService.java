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
    private final TaskContextStore contextStore;

    public MockChatService(TaskContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @Override
    public SseEmitter streamTask(TaskStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        executor.execute(() -> sendMockResponse(emitter, request));
        return emitter;
    }

    private void sendMockResponse(SseEmitter emitter, TaskStreamRequest request) {
        List<SectionSelection> selections = resolveSelections(request);
        List<String> rows = selections.stream()
                .map(selected -> selected.sectionPath() != null ? selected.sectionPath() : "Ganzes Dokument")
                .map(label -> "• " + label)
                .toList();

        String preamble = "Aufgabe für \"" + request.prompt() + "\"";
        String details = rows.isEmpty() ? "Keine Abschnitte ausgewählt." : String.join("\n", rows);
        String closing = "Mock-Antwort basierend auf den ausgewählten Abschnitten.";

        try {
            emitter.send(SseEmitter.event().name("message").data(preamble));
            pause();
            emitter.send(SseEmitter.event().name("message").data(details));
            pause();
            emitter.send(SseEmitter.event().name("message").data(closing));
            emitter.send(SseEmitter.event().name("close").data("close"));
            emitter.complete();
        } catch (IOException e) {
            LOGGER.warn("Failed to send SSE response", e);
            emitter.completeWithError(e);
        }
    }

    private List<SectionSelection> resolveSelections(TaskStreamRequest request) {
        List<SectionSelection> cachedSelections = contextStore.take(request.contextToken());
        if (!cachedSelections.isEmpty()) {
            return cachedSelections;
        }

        if (!request.sectionIds().isEmpty()) {
            return request.sectionIds().stream()
                    .map(id -> new SectionSelection(null, null, null, id, "Abschnitt " + id, null))
                    .toList();
        }

        if (!request.documentIds().isEmpty()) {
            return request.documentIds().stream()
                    .map(docId -> new SectionSelection(docId, null, null, null, null, null))
                    .toList();
        }

        return List.of();
    }

    private void pause() {
        try {
            Thread.sleep(Duration.ofMillis(300));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
