package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface that scores passage candidates with a cross encoder model.
 * Implementations may contact a remote inference endpoint or apply lightweight
 * heuristics. The {@link #score(String, List)} method returns a map that
 * associates the chunk id with the computed score.
 */
public interface CrossEncoderReranker {

    Map<Long, Double> score(String question, List<ContextCandidate> candidates);

    /**
     * Lightweight view of a context chunk that is passed to the reranker.
     */
    record ContextCandidate(long chunkId, String content, double lexicalScore, double semanticScore) {
    }
}
