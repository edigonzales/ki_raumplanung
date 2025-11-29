package ch.so.arp.rag.chat;

import java.util.List;

/**
 * Lightweight replacement for the PostgreSQL based vector database. It
 * deterministically mirrors the question so that unit tests can run without a
 * running database.
 */
class MockVectorDatabase implements VectorDatabase {

    @Override
    public List<RetrievedContext> findRelevantContext(String question, int limit) {
        return List.of(new RetrievedContext(0L, "mock-task", "Mock heading", "", "",
                "Mock context for: " + question, 0.0d, 0.0d, 0.0d, 0.0d));
    }
}
