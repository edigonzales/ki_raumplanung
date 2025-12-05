package ch.so.arp.rag.chat;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record SummaryStreamRequest(
        @NotBlank String prompt,
        List<Long> sectionIds,
        List<UUID> documentIds) {

    public SummaryStreamRequest {
        sectionIds = sectionIds == null ? List.of() : sectionIds;
        documentIds = documentIds == null ? List.of() : documentIds;
    }
}
