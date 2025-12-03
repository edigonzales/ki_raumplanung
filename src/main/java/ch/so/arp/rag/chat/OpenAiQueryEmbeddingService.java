package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.util.StringUtils;

public class OpenAiQueryEmbeddingService implements QueryEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public OpenAiQueryEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public Optional<List<Double>> embed(String text) {
        if (!StringUtils.hasText(text)) {
            return Optional.empty();
        }

        float[] vector = embeddingModel.embed(text);
        if (vector == null || vector.length == 0) {
            return Optional.empty();
        }

        return Optional.of(floatArrayToList(vector));
    }

    private List<Double> floatArrayToList(float[] values) {
        List<Double> result = new java.util.ArrayList<>(values.length);
        for (float value : values) {
            result.add((double) value);
        }
        return result;
    }
}
