package ch.so.arp.rag.chat;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST endpoint exposing the chat functionality via server sent events.
 */
@RestController
@RequestMapping(path = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
@Validated
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/summary-stream")
    public SseEmitter streamSummary(@Valid @ModelAttribute SummaryStreamRequest request) {
        LOGGER.info(
                "Starting summary stream for {} sections across {} documents",
                request.sectionIds().size(),
                request.documentIds().size());
        return chatService.streamSummary(request);
    }
}
