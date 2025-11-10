package ch.so.arp.rag.chat;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final SseEmitterFactory emitterFactory;

    public ChatController(ChatService chatService, SseEmitterFactory emitterFactory) {
        this.chatService = chatService;
        this.emitterFactory = emitterFactory;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = emitterFactory.create();
        chatService.streamAnswer(request.question(), new ChatService.StreamingResponseHandler() {
            @Override
            public void onToken(String token) {
                try {
                    emitter.send(token);
                } catch (IOException ex) {
                    LOGGER.warn("Unable to stream token: {}", token, ex);
                    emitter.completeWithError(ex);
                }
            }

            @Override
            public void onComplete() {
                emitter.complete();
            }

            @Override
            public void onError(Throwable throwable) {
                emitter.completeWithError(throwable);
            }
        });
        return emitter;
    }
}
