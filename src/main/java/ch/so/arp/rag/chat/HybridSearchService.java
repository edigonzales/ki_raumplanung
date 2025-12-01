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

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Mock implementation of a hybrid search combining keyword and embedding matches.
 */
@Service
public class HybridSearchService {

    private final Map<Long, DocumentChunk> indexedChunks = new ConcurrentHashMap<>();

    /**
     * Example hybrid search query for PostgreSQL (schema: {@code arp_rag_vp}).
     * <p>
     * It blends full-text ranking with vector similarity and keeps the scoring
     * logic in SQL so the Java layer can stay thin.
     * </p>
     */
    public static final String HYBRID_SEARCH_SQL = """
            WITH params AS (
                SELECT
                    websearch_to_tsquery('german', :keywords)     AS q,
                    :embedding::halfvec                           AS emb,
                    :municipality                                 AS municipality,
                    :plan_type                                    AS plan_type,
                    COALESCE(:limit, 20)                           AS limit
            ), ranked AS (
                SELECT
                    c.id,
                    c.document_id,
                    d.filename,
                    d.title,
                    s.section_path,
                    substring(c.text FROM 1 FOR 240) AS snippet,
                    ts_rank_cd(c.tsv, p.q)           AS keyword_score,
                    1 - (c.embedding <=> p.emb)      AS vector_score,
                    (0.6 * ts_rank_cd(c.tsv, p.q) + 0.4 * (1 - (c.embedding <=> p.emb))) AS hybrid_score
                FROM arp_rag_vp.chunks c
                JOIN arp_rag_vp.documents d ON d.id = c.document_id
                LEFT JOIN arp_rag_vp.sections s ON s.id = c.section_id
                CROSS JOIN params p
                WHERE (
                        (p.q IS NOT NULL AND c.tsv @@ p.q)
                     OR (p.emb IS NOT NULL AND c.embedding <=> p.emb < 0.4)
                    )
                  AND (p.municipality IS NULL OR c.municipality = p.municipality)
                  AND (p.plan_type IS NULL OR c.plan_type = p.plan_type)
            )
            SELECT id, document_id, filename, title, section_path, snippet,
                   keyword_score, vector_score, hybrid_score
            FROM ranked
            ORDER BY hybrid_score DESC NULLS LAST, id ASC
            LIMIT (SELECT limit FROM params);
            """;

    public HybridSearchService() {
        seedData();
    }

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

    public List<DocumentChunk> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(indexedChunks::get)
                .filter(chunk -> chunk != null)
                .collect(Collectors.toList());
    }

    /**
     * Exposes the hybrid SQL so it can be displayed in documentation or logs.
     */
    public String hybridSearchSql() {
        return HYBRID_SEARCH_SQL;
    }

    private void seedData() {
        UUID doc1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID doc2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        addChunk(1, doc1, "naturwerte.pdf", "Naturwerte", "2 > Landschaft > Naturwerte > Hecken",
                "Die Hecke entlang des Weges bildet einen wertvollen Lebensraum und ist zu erhalten.");
        addChunk(2, doc1, "naturwerte.pdf", "Naturwerte", "2 > Landschaft > Naturwerte > Gewässer",
                "Das Bachufer ist als Vernetzungsachse zu stärken und darf nicht verbaut werden.");
        addChunk(3, doc2, "gestaltung.pdf", "Gestaltungsplan", "1 > Freiraum > Pflanzkonzept",
                "Hecken mit einheimischen Sträuchern strukturieren den Spielplatz und bieten Sichtschutz.");
        addChunk(4, doc2, "gestaltung.pdf", "Gestaltungsplan", "3 > Verkehr > Fusswege",
                "Neue Fusswegverbindungen sind begrünt und mit Bäumen begleitet.");
    }

    private void addChunk(long id, UUID documentId, String filename, String title, String path, String snippet) {
        indexedChunks.put(id, new DocumentChunk(id, documentId, filename, title, path, snippet));
    }

    private double mockVectorScore(String haystack, String needle) {
        int distance = Math.abs(haystack.length() - haystack.replace(needle, "").length());
        return Math.min(1.0, distance / (double) (haystack.length() + 1));
    }
}
