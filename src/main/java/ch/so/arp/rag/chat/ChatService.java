package ch.so.arp.rag.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    SseEmitter streamTask(TaskStreamRequest request);
}
