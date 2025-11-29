package ch.so.arp.rag.chat;

/**
 * Strategy abstraction used to compute embeddings for questions. Implementations
 * can either call a remote embedding API or provide deterministic placeholders
 * that are suited for tests and local development.
 */
public interface EmbeddingProvider {

    /**
     * Create an embedding vector for the provided text.
     *
     * @param text the text to embed
     * @return the embedding represented as a float array
     */
    float[] embed(String text);
}
