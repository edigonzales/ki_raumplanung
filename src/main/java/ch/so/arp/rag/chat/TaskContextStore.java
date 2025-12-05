package ch.so.arp.rag.chat;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class TaskContextStore {

    private final ConcurrentMap<UUID, List<SectionSelection>> cache = new ConcurrentHashMap<>();

    public UUID store(List<SectionSelection> selections) {
        UUID token = UUID.randomUUID();
        cache.put(token, List.copyOf(selections));
        return token;
    }

    public List<SectionSelection> take(UUID token) {
        if (token == null) {
            return List.of();
        }
        List<SectionSelection> removed = cache.remove(token);
        return removed == null ? Collections.emptyList() : removed;
    }
}
