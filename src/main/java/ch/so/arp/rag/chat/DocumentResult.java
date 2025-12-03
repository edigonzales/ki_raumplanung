package ch.so.arp.rag.chat;

import java.util.ArrayList;
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
        List<String> chunkTexts = sectionChunks.stream()
                .map(DocumentChunk::text)
                .filter(Objects::nonNull)
                .map(DocumentResult::normalizeWhitespace)
                .filter(text -> !text.isBlank())
                .toList();

        if (chunkTexts.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder merged = new StringBuilder(chunkTexts.getFirst());
        for (int i = 1; i < chunkTexts.size(); i++) {
            String current = chunkTexts.get(i);
            int overlap = findCharacterOverlap(merged, current);
            if (overlap == 0 && needsPadding(merged, current)) {
                merged.append(' ');
            }
            merged.append(current, overlap, current.length());
        }

        return Optional.of(merged.toString());
    }

    private static String normalizeWhitespace(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    private static boolean needsPadding(StringBuilder merged, String current) {
        return !merged.isEmpty() && !Character.isWhitespace(merged.charAt(merged.length() - 1))
                && !current.isEmpty() && !Character.isWhitespace(current.charAt(0));
    }

    private static int findCharacterOverlap(StringBuilder merged, String current) {
        int maxOverlap = Math.min(merged.length(), current.length());
        final int minOverlap = 8;
        for (int candidate = maxOverlap; candidate >= minOverlap; candidate--) {
            int mergedStart = merged.length() - candidate;
            if (mergedStart < 0) {
                continue;
            }
            if (regionMatchesIgnoreCase(merged, mergedStart, current, 0, candidate)) {
                return candidate;
            }
        }
        return 0;
    }

    private static boolean regionMatchesIgnoreCase(CharSequence left, int leftStart, CharSequence right, int rightStart, int length) {
        for (int i = 0; i < length; i++) {
            char l = left.charAt(leftStart + i);
            char r = right.charAt(rightStart + i);
            if (Character.toLowerCase(l) != Character.toLowerCase(r)) {
                return false;
            }
        }
        return true;
    }
}
