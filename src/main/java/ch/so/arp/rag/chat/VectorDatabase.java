package ch.so.arp.rag.chat;

import java.util.List;

/**
 * Minimal vector database abstraction. It exposes the capability to retrieve
 * textual context that is relevant to the provided question.
 */
public interface VectorDatabase {

    /**
     * Find relevant context snippets for the question.
     *
     * @param question the question that should be answered
     * @param limit    the maximum amount of snippets to return
     * @return a list of context snippets enriched with metadata
     */
    List<RetrievedContext> findRelevantContext(String question, int limit);
}
