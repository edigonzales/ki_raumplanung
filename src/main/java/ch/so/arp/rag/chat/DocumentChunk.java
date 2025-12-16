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
        Long sectionId,
        String sectionPath,
        String text,
        String sectionText,
        String snippet,
        String municipality,
        String planType,
        Double keywordScore,
        Double vectorScore,
        Double hybridScore) {
}
