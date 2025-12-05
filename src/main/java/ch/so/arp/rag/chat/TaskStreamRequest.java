package ch.so.arp.rag.chat;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record TaskStreamRequest(
        @NotBlank String prompt,
        List<Long> sectionIds,
        List<UUID> documentIds) {

    public TaskStreamRequest {
        sectionIds = sectionIds == null ? List.of() : sectionIds;
        documentIds = documentIds == null ? List.of() : documentIds;
    }
}
