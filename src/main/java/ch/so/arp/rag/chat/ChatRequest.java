package ch.so.arp.rag.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming payload for chat requests.
 */
public record ChatRequest(@NotBlank String question) {
}
