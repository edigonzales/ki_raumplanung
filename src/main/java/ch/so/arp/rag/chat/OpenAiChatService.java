package ch.so.arp.rag.chat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class OpenAiChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiChatService.class);

    private final ChatClient chatClient;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public OpenAiChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public SseEmitter streamSummary(SummaryStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        executor.execute(() -> sendResponse(emitter, request));
        return emitter;
    }

    private void sendResponse(SseEmitter emitter, SummaryStreamRequest request) {
        try {
            String prompt = buildPrompt(request);
            String content = chatClient.prompt().user(prompt).call().content();
            emitter.send(SseEmitter.event().name("message").data(content));
            emitter.complete();
        } catch (Exception e) {
            LOGGER.warn("Failed to stream OpenAI response, falling back to error", e);
            try {
                emitter.send(SseEmitter.event().name("message").data("Keine Antwort vom LLM verfügbar."));
                emitter.completeWithError(e);
            } catch (IOException ioException) {
                emitter.completeWithError(ioException);
            }
        }
    }

    private String buildPrompt(SummaryStreamRequest request) {
        List<String> sections = request.selection().stream()
                .map(id -> "Abschnitt " + id)
                .toList();

        return "Erstelle eine Zusammenfassung basierend auf folgenden Abschnitten: "
                + String.join(", ", sections)
                + ". Nutzeranfrage: " + request.prompt();
    }

}
