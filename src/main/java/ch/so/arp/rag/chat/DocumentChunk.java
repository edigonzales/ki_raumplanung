package ch.so.arp.rag.chat;

import java.util.UUID;

/**
 * Simple representation of a chunked document section used by the mock search.
 */
public record DocumentChunk(
        long id,
        UUID documentId,
        String filename,
        String title,
        String sectionPath,
        String snippet,
        String municipality,
        String planType,
        Double keywordScore,
        Double vectorScore,
        Double hybridScore) {
}
