package ch.so.arp.rag.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Factory abstraction to create {@link SseEmitter} instances. This indirection makes
 * it easier to unit test components that rely on server sent events.
 */
@FunctionalInterface
public interface SseEmitterFactory {

    /**
     * Create a new emitter instance.
     *
     * @return a fresh {@link SseEmitter}
     */
    SseEmitter create();
}
