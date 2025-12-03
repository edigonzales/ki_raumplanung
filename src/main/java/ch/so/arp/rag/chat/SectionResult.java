package ch.so.arp.rag.chat;

import java.util.List;

public record SectionResult(
        Long sectionId,
        String sectionPath,
        String sectionText,
        List<Long> chunkIds,
        Double bestHybridScore) {
}
