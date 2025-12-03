package ch.so.arp.rag.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

/**
 * Mock implementation of a hybrid search combining keyword and embedding matches.
 */
public class MockHybridSearchService implements DocumentSearchService {

    private final Map<Long, DocumentChunk> indexedChunks = new ConcurrentHashMap<>();

    public MockHybridSearchService() {
        seedData();
    }

    @Override
    public List<DocumentChunk> search(String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return Collections.emptyList();
        }

        String normalized = keywords.toLowerCase(Locale.ROOT);
        List<DocumentChunk> matches = new ArrayList<>();
        for (DocumentChunk chunk : indexedChunks.values()) {
            String haystack = (chunk.title() + " " + chunk.sectionPath() + " " + chunk.snippet()).toLowerCase(Locale.ROOT);
            if (haystack.contains(normalized)) {
                matches.add(chunk);
                continue;
            }

            if (mockVectorScore(haystack, normalized) > 0.4) {
                matches.add(chunk);
            }
        }

        return matches.stream()
                .sorted(Comparator.comparing(DocumentChunk::id))
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentChunk> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(indexedChunks::get)
                .filter(chunk -> chunk != null)
                .collect(Collectors.toList());
    }

    @Override
    public String hybridSearchSql() {
        return HybridSearchSql.HYBRID_SEARCH_SQL;
    }

    private void seedData() {
        UUID doc1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID doc2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        addChunk(
                1,
                doc1,
                "naturwerte.pdf",
                "Naturwerte",
                "2 > Landschaft > Naturwerte > Hecken",
                "Die Hecke entlang des Weges bildet einen wertvollen Lebensraum und ist zu erhalten.",
                "Solothurn",
                "Gestaltungsplan",
                0.82);
        addChunk(
                2,
                doc1,
                "naturwerte.pdf",
                "Naturwerte",
                "2 > Landschaft > Naturwerte > Gewässer",
                "Das Bachufer ist als Vernetzungsachse zu stärken und darf nicht verbaut werden.",
                "Solothurn",
                "Gestaltungsplan",
                0.73);
        addChunk(
                3,
                doc2,
                "gestaltung.pdf",
                "Gestaltungsplan",
                "1 > Freiraum > Pflanzkonzept",
                "Hecken mit einheimischen Sträuchern strukturieren den Spielplatz und bieten Sichtschutz.",
                "Olten",
                "Sondernutzungsplan",
                0.77);
        addChunk(
                4,
                doc2,
                "gestaltung.pdf",
                "Gestaltungsplan",
                "3 > Verkehr > Fusswege",
                "Neue Fusswegverbindungen sind begrünt und mit Bäumen begleitet.",
                "Olten",
                "Sondernutzungsplan",
                0.61);
    }

    private void addChunk(
            long id,
            UUID documentId,
            String filename,
            String title,
            String path,
            String snippet,
            String municipality,
            String planType,
            Double hybridScore) {
        indexedChunks.put(
                id,
                new DocumentChunk(id, documentId, filename, title, path, snippet, municipality, planType, hybridScore));
    }

    private double mockVectorScore(String haystack, String needle) {
        int distance = Math.abs(haystack.length() - haystack.replace(needle, "").length());
        return Math.min(1.0, distance / (double) (haystack.length() + 1));
    }
}
