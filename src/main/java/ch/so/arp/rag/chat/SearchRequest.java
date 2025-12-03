package ch.so.arp.rag.chat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank String keywords,
        @DecimalMin("0.0") @DecimalMax("1.0") double lexicalWeight) {
}
