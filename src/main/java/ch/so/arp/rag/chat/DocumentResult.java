package ch.so.arp.rag.chat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public record DocumentResult(
        UUID documentId,
        String filename,
        String title,
        String municipality,
        String planType,
        List<SectionResult> sections,
        Double bestHybridScore,
        List<Long> chunkIds) {

    public static List<DocumentResult> fromChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<DocumentChunk>> groupedByDocument = chunks.stream()
                .collect(Collectors.groupingBy(DocumentChunk::documentId));

        List<DocumentResult> documents = new ArrayList<>();
        for (Map.Entry<UUID, List<DocumentChunk>> entry : groupedByDocument.entrySet()) {
            List<DocumentChunk> documentChunks = entry.getValue();
            if (documentChunks.isEmpty()) {
                continue;
            }

            List<SectionResult> sectionResults = buildSections(documentChunks);
            Double documentScore = highestHybridScore(documentChunks);
            List<Long> docChunkIds = documentChunks.stream().map(DocumentChunk::id).toList();
            DocumentChunk firstChunk = documentChunks.getFirst();

            documents.add(new DocumentResult(
                    entry.getKey(),
                    firstChunk.filename(),
                    firstChunk.title(),
                    firstChunk.municipality(),
                    firstChunk.planType(),
                    sectionResults,
                    documentScore,
                    docChunkIds));
        }

        documents.sort(
                Comparator.comparing(DocumentResult::bestHybridScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(DocumentResult::title, Comparator.nullsLast(String::compareToIgnoreCase)));
        return documents;
    }

    private static List<SectionResult> buildSections(List<DocumentChunk> documentChunks) {
        record SectionKey(Long id, String path) {
        }

        Map<SectionKey, List<DocumentChunk>> groupedSections = documentChunks.stream()
                .collect(Collectors.groupingBy(chunk -> new SectionKey(chunk.sectionId(), chunk.sectionPath())));

        List<SectionResult> sections = new ArrayList<>();
        for (Map.Entry<SectionKey, List<DocumentChunk>> entry : groupedSections.entrySet()) {
            List<DocumentChunk> sectionChunks = entry.getValue();
            Double sectionScore = highestHybridScore(sectionChunks);
            List<Long> chunkIds = sectionChunks.stream().map(DocumentChunk::id).toList();
            sections.add(new SectionResult(
                    entry.getKey().id(),
                    entry.getKey().path(),
                    resolveSectionText(sectionChunks),
                    chunkIds,
                    sectionScore));
        }

        sections.sort(
                Comparator.comparing(SectionResult::bestHybridScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SectionResult::sectionPath, Comparator.nullsLast(String::compareToIgnoreCase)));
        return sections;
    }

    private static Double highestHybridScore(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(DocumentChunk::hybridScore)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    private static String resolveSectionText(List<DocumentChunk> sectionChunks) {
        return sectionChunks.stream()
                .map(DocumentChunk::sectionText)
                .filter(Objects::nonNull)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElseGet(() -> sectionChunks.stream()
                        .map(DocumentChunk::snippet)
                        .filter(Objects::nonNull)
                        .filter(text -> !text.isBlank())
                        .collect(Collectors.joining(" ")));
    }
}
