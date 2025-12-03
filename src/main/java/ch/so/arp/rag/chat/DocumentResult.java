package ch.so.arp.rag.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
            List<DocumentChunk> sectionChunks = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparingLong(DocumentChunk::id))
                    .toList();
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
        Optional<String> mergedFromChunks = mergeChunkTexts(sectionChunks);
        return mergedFromChunks
                .filter(text -> !text.isBlank())
                .orElseGet(() -> sectionChunks.stream()
                        .map(DocumentChunk::sectionText)
                        .filter(Objects::nonNull)
                        .filter(text -> !text.isBlank())
                        .findFirst()
                        .orElseGet(() -> sectionChunks.stream()
                                .map(DocumentChunk::snippet)
                                .filter(Objects::nonNull)
                                .filter(text -> !text.isBlank())
                                .collect(Collectors.joining(" "))));
    }

    private static Optional<String> mergeChunkTexts(List<DocumentChunk> sectionChunks) {
        List<List<String>> chunkWords = sectionChunks.stream()
                .map(DocumentChunk::text)
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(text -> !text.isBlank())
                .map(text -> (List<String>) Arrays.asList(text.split("\\s+")))
                .filter(words -> !words.isEmpty())
                .toList();

        if (chunkWords.isEmpty()) {
            return Optional.empty();
        }

        List<String> merged = new ArrayList<>(chunkWords.getFirst());
        for (int i = 1; i < chunkWords.size(); i++) {
            List<String> current = chunkWords.get(i);
            int maxOverlap = Math.min(merged.size(), current.size());
            int overlap = 0;
            for (int candidate = maxOverlap; candidate >= 5; candidate--) {
                if (merged.subList(merged.size() - candidate, merged.size()).equals(current.subList(0, candidate))) {
                    overlap = candidate;
                    break;
                }
            }
            merged.addAll(current.subList(overlap, current.size()));
        }

        return Optional.of(String.join(" ", merged));
    }
}
