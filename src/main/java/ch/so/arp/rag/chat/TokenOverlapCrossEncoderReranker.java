package ch.so.arp.rag.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deterministic cross encoder that estimates the relevance of a candidate by
 * measuring the token overlap with the question. Although it does not replace a
 * real machine learning model, the heuristic behaves similarly to a
 * cross-encoder reranker and therefore acts as a useful placeholder for local
 * development and automated tests.
 */
class TokenOverlapCrossEncoderReranker implements CrossEncoderReranker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenOverlapCrossEncoderReranker.class);

    @Override
    public Map<Long, Double> score(String question, List<ContextCandidate> candidates) {
        Set<String> questionTokens = tokenize(question);
        Map<Long, Double> scores = new HashMap<>();
        for (ContextCandidate candidate : candidates) {
            Set<String> passageTokens = tokenize(candidate.content());
            long overlap = passageTokens.stream().filter(questionTokens::contains).count();
            double lexicalBoost = Math.max(0.0d, candidate.lexicalScore());
            double semanticBoost = Math.max(0.0d, candidate.semanticScore());
            double normalizedOverlap = questionTokens.isEmpty() ? 0.0d
                    : (double) overlap / (double) questionTokens.size();
            double score = normalizedOverlap + 0.1d * lexicalBoost + 0.2d * semanticBoost;
            scores.put(candidate.chunkId(), score);
        }
        LOGGER.debug("Cross encoder scored {} candidates", candidates.size());
        return scores;
    }

    private Set<String> tokenize(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        Set<String> tokens = new HashSet<>();
        for (String token : normalized.split("\\W+")) {
            if (token.length() >= 3) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
