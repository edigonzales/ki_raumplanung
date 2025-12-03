package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Optional;

/**
 * Provides vector embeddings for user queries when an embedding model is
 * available. Implementations are expected to return {@link Optional#empty()}
 * when embedding generation is not possible so callers can gracefully fall
 * back to keyword-only search.
 */
public interface QueryEmbeddingService {

    Optional<List<Double>> embed(String text);
}
