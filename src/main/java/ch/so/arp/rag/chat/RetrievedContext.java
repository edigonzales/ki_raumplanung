package ch.so.arp.rag.chat;

import java.util.StringJoiner;

/**
 * Result element returned by the vector database after hybrid retrieval,
 * fusion and reranking.
 */
public record RetrievedContext(
        long chunkId,
        String taskName,
        String heading,
        String url,
        String anchor,
        String content,
        double lexicalScore,
        double semanticScore,
        double fusedScore,
        double crossEncoderScore) {

    /**
     * Formats the context in a compact representation that can be used inside a
     * prompt. The formatting keeps important metadata close to the text so that
     * downstream components such as the LLM can refer to the original source.
     */
    public String formatForPrompt() {
        StringJoiner joiner = new StringJoiner("\n");
        if (!heading.isBlank()) {
            joiner.add("Titel: " + heading);
        }
        if (!taskName.isBlank()) {
            joiner.add("Dokument: " + taskName);
        }
        if (!anchor.isBlank()) {
            joiner.add("Abschnitt: " + anchor);
        }
        joiner.add(content);
        if (!url.isBlank()) {
            joiner.add("Quelle: " + url);
        }
        return joiner.toString();
    }
}
