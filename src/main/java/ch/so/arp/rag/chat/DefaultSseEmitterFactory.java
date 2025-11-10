package ch.so.arp.rag.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Default factory creating emitters with no timeout so that long running
 * conversations can finish without interruption.
 */
class DefaultSseEmitterFactory implements SseEmitterFactory {

    private static final Long NO_TIMEOUT = 0L;

    @Override
    public SseEmitter create() {
        return new SseEmitter(NO_TIMEOUT);
    }
}
