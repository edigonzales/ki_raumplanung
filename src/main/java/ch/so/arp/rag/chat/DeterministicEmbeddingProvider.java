package ch.so.arp.rag.chat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deterministic embedding provider that mirrors the dummy embedding logic used
 * during the ingestion process. It allows the application to run without
 * external API calls while still supporting semantic search queries.
 */
class DeterministicEmbeddingProvider implements EmbeddingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeterministicEmbeddingProvider.class);

    private final int dimensions;

    DeterministicEmbeddingProvider(int dimensions) {
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        this.dimensions = dimensions;
        LOGGER.info("Using deterministic embeddings with {} dimensions", dimensions);
    }

    @Override
    public float[] embed(String text) {
        byte[] seed = sha256(text);
        Random random = new Random(bytesToLong(seed));
        float[] vector = new float[dimensions];
        double norm = 0.0d;
        for (int i = 0; i < vector.length; i++) {
            float value = (random.nextFloat() * 2.0f) - 1.0f;
            vector[i] = value;
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private long bytesToLong(byte[] bytes) {
        long result = 0L;
        for (int i = 0; i < Math.min(8, bytes.length); i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        return result;
    }
}
