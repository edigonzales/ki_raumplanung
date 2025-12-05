package ch.so.arp.rag.chat;

import java.util.UUID;

public record SectionSelection(
        UUID documentId,
        String filename,
        String title,
        Long sectionId,
        String sectionPath,
        String text) {
}
