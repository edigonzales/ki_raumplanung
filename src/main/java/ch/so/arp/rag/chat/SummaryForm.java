package ch.so.arp.rag.chat;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record SummaryForm(
        @NotBlank String prompt,
        List<Long> sectionIds,
        boolean useFullDocuments) {
}
