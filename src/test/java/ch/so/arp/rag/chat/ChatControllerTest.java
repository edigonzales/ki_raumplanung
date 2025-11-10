package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class ChatControllerTest {

    @Test
    void streamsResponsesThroughSseEmitter() throws IOException {
        ChatService chatService = mock(ChatService.class);
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        AtomicReference<ChatService.StreamingResponseHandler> handlerReference = new AtomicReference<>();
        doAnswer(invocation -> {
            handlerReference.set(invocation.getArgument(1));
            return null;
        }).when(chatService).streamAnswer(eq("How are you?"), any());

        ChatController controller = new ChatController(chatService, () -> emitter);
        SseEmitter returnedEmitter = controller.chat(new ChatRequest("How are you?"));

        assertThat(returnedEmitter).isSameAs(emitter);
        ChatService.StreamingResponseHandler handler = handlerReference.get();
        handler.onToken("token-1");
        handler.onToken("token-2");
        handler.onComplete();

        assertThat(emitter.getEvents()).containsExactly("token-1", "token-2");
        assertThat(emitter.isCompleted()).isTrue();
    }

    @Test
    void reportsErrorsFromStreamingHandler() {
        ChatService chatService = mock(ChatService.class);
        RecordingSseEmitter emitter = new RecordingSseEmitter();
        AtomicReference<ChatService.StreamingResponseHandler> handlerReference = new AtomicReference<>();
        doAnswer(invocation -> {
            handlerReference.set(invocation.getArgument(1));
            return null;
        }).when(chatService).streamAnswer(eq("broken"), any());

        ChatController controller = new ChatController(chatService, () -> emitter);
        controller.chat(new ChatRequest("broken"));

        RuntimeException failure = new RuntimeException("boom");
        handlerReference.get().onError(failure);

        assertThat(emitter.getErrors()).containsExactly(failure);
        assertThat(emitter.isCompleted()).isFalse();
    }

    private static final class RecordingSseEmitter extends SseEmitter {

        private final List<String> events = new CopyOnWriteArrayList<>();
        private final List<Throwable> errors = new CopyOnWriteArrayList<>();
        private volatile boolean completed;

        private RecordingSseEmitter() {
            super(0L);
        }

        @Override
        public void send(Object object) throws IOException {
            events.add(String.valueOf(object));
        }

        @Override
        public void complete() {
            completed = true;
            super.complete();
        }

        @Override
        public void completeWithError(Throwable ex) {
            errors.add(ex);
            super.completeWithError(ex);
        }

        List<String> getEvents() {
            return new ArrayList<>(events);
        }

        List<Throwable> getErrors() {
            return new ArrayList<>(errors);
        }

        boolean isCompleted() {
            return completed;
        }
    }
}
