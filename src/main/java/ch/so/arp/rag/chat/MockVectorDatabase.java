package ch.so.arp.rag.chat;

import java.util.List;

/**
 * Lightweight replacement for the PostgreSQL based vector database. It
 * deterministically mirrors the question so that unit tests can run without a
 * running database.
 */
class MockVectorDatabase implements VectorDatabase {

    @Override
    public List<String> findRelevantContext(String question) {
        return List.of("Mock context for: " + question);
    }
}
