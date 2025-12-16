package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Optional;

public class MockQueryEmbeddingService implements QueryEmbeddingService {

    @Override
    public Optional<List<Double>> embed(String text) {
        return Optional.empty();
    }
}
