package ch.so.arp.rag.chat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class OpenAiChatService implements ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiChatService.class);

    private final ChatClient chatClient;
    private final DocumentSearchService searchService;
    private final PromptFactory promptFactory;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public OpenAiChatService(ChatClient chatClient, DocumentSearchService searchService, PromptFactory promptFactory) {
        this.chatClient = chatClient;
        this.searchService = searchService;
        this.promptFactory = promptFactory;
    }

    @Override
    public SseEmitter streamTask(TaskStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        executor.execute(() -> sendResponse(emitter, request));
        return emitter;
    }

    private void sendResponse(SseEmitter emitter, TaskStreamRequest request) {
        try {
            String prompt = buildPrompt(request);
            String content = chatClient.prompt().system(promptFactory.buildSystemPromptWithoutUserQuestion(prompt)).user(request.prompt()).call().content();
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

    private String buildPrompt(TaskStreamRequest request) {
        boolean useFullDocuments = !request.documentIds().isEmpty();
        List<SectionSelection> selections = searchService.findBySectionSelections(request.sectionIds(), useFullDocuments);

        if (selections.isEmpty()) {
            return "Kein Kontext verfügbar.";
        }

        return selections.stream()
                .map(selection -> formatSelection(selection, useFullDocuments))
                .collect(Collectors.joining("\n\n"));
    }

    private String formatSelection(SectionSelection selection, boolean useFullDocuments) {
        String scopeLabel = useFullDocuments || selection.sectionId() == null
                ? "Ganzes Dokument"
                : selection.sectionPath();
        String title = selection.title() != null ? selection.title() : "Unbenanntes Dokument";
        String documentInfo = selection.filename() != null ? selection.filename() : "Datei unbekannt";
        return "Titel: " + title
                + " (" + documentInfo + ")\n"
                + "Abschnitt: " + (scopeLabel == null ? "Unbekannt" : scopeLabel) + "\n"
                + "Text:\n" + selection.text();
    }
}
