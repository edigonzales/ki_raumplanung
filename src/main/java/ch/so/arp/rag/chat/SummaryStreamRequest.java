package ch.so.arp.rag.chat;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record SummaryStreamRequest(
        @NotBlank String prompt,
        @NotEmpty List<Long> selection) {
}
