package ch.so.arp.rag.chat;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(@NotBlank String keywords) {
}
