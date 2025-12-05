package ch.so.arp.rag.chat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.util.StringUtils;

public class JdbcHybridSearchService implements DocumentSearchService {

    private final JdbcClient jdbcClient;
    private final QueryEmbeddingService embeddingService;

    public JdbcHybridSearchService(JdbcClient jdbcClient, QueryEmbeddingService embeddingService) {
        this.jdbcClient = jdbcClient;
        this.embeddingService = embeddingService;
    }

    @Override
    public List<DocumentResult> search(String keywords, double lexicalWeight, String municipality, String planType) {
        if (!StringUtils.hasText(keywords)) {
            return Collections.emptyList();
        }

        SqlParameterSource params = buildSearchParams(keywords, lexicalWeight, municipality, planType);
        
        List<DocumentChunk> chunks = jdbcClient.sql(HybridSearchSql.HYBRID_SEARCH_SQL)
                .paramSource(params)
                .query(DataClassRowMapper.newInstance(DocumentChunk.class))
                .list();
    
//        for (var chunk : chunks) {
//            System.out.println(chunk.filename() + " --- " + chunk.hybridScore() + " --- " + chunk.keywordScore() + " --- " + chunk.vectorScore());
//        }
        
        return DocumentResult.fromChunks(chunks);
    }

    @Override
    public List<SectionSelection> findBySectionSelections(List<Long> sectionIds, List<UUID> documentIds) {
        boolean hasSections = sectionIds != null && !sectionIds.isEmpty();
        boolean hasDocuments = documentIds != null && !documentIds.isEmpty();

        if (!hasSections && !hasDocuments) {
            return Collections.emptyList();
        }

        List<SectionSelection> selections = new java.util.ArrayList<>();

        if (hasSections) {
            String sectionSql = """
                    SELECT
                        c.document_id,
                        d.filename,
                        d.title,
                        c.section_id,
                        s.section_path,
                        string_agg(c.text, ' ' ORDER BY c.id) AS text
                    FROM arp_rag_vp.chunks c
                    JOIN arp_rag_vp.documents d ON d.id = c.document_id
                    LEFT JOIN arp_rag_vp.sections s ON s.id = c.section_id
                    WHERE c.section_id IN (:sectionIds)
                    GROUP BY c.document_id, d.filename, d.title, c.section_id, s.section_path
                    ORDER BY d.title, s.section_path
                    """;

            selections.addAll(jdbcClient.sql(sectionSql)
                    .param("sectionIds", sectionIds)
                    .query(DataClassRowMapper.newInstance(SectionSelection.class))
                    .list());
        }

        if (hasDocuments) {
            String documentSql = """
                    SELECT
                        d.id AS document_id,
                        d.filename,
                        d.title,
                        NULL::bigint AS section_id,
                        NULL::text AS section_path,
                        string_agg(c.text, ' ' ORDER BY c.id) AS text
                    FROM arp_rag_vp.documents d
                    JOIN arp_rag_vp.chunks c ON c.document_id = d.id
                    WHERE d.id IN (:documentIds)
                    GROUP BY d.id, d.filename, d.title
                    ORDER BY d.title
                    """;

            selections.addAll(jdbcClient.sql(documentSql)
                    .param("documentIds", documentIds)
                    .query(DataClassRowMapper.newInstance(SectionSelection.class))
                    .list());
        }

        selections.sort(java.util.Comparator.comparing(SectionSelection::title, java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(selection -> selection.sectionPath() == null ? "" : selection.sectionPath(), String::compareToIgnoreCase));

        return selections;
    }

    @Override
    public String hybridSearchSql() {
        return HybridSearchSql.HYBRID_SEARCH_SQL;
    }

    MapSqlParameterSource buildSearchParams(String keywords, double lexicalWeight, String municipality, String planType) {
        String normalizedKeywords = normalizeKeywords(keywords);
        return new MapSqlParameterSource()
                .addValue("keywords", normalizedKeywords)
                .addValue(
                        "embedding",
                        embeddingService.embed(buildEmbeddingInput(keywords)).map(this::toHalfvecLiteral).orElse(null))
                .addValue("lexical_weight", normalizeLexicalWeight(lexicalWeight))
                .addValue("municipality", normalizeFilter(municipality))
                .addValue("plan_type", normalizeFilter(planType))
                .addValue("limit", 2000);
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    double normalizeLexicalWeight(double lexicalWeight) {
        if (Double.isNaN(lexicalWeight)) {
            return 0.6d;
        }
        if (lexicalWeight < 0.0d) {
            return 0.0d;
        }
        if (lexicalWeight > 1.0d) {
            return 1.0d;
        }
        return lexicalWeight;
    }

    private String normalizeKeywords(String keywords) {
        List<String> terms = splitKeywords(keywords);
        if (terms.isEmpty()) {
            return keywords;
        }
        if (terms.size() == 1) {
            return terms.getFirst();
        }
        return String.join(" OR ", terms);
    }

    private String buildEmbeddingInput(String keywords) {
        List<String> terms = splitKeywords(keywords);
        return terms.isEmpty() ? keywords : String.join(" ", terms);
    }

    private List<String> splitKeywords(String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return List.of();
        }
        return List.of(keywords.split(","))
                .stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String toHalfvecLiteral(List<Double> embedding) {
        return embedding == null || embedding.isEmpty() ? null
                : embedding.stream()
                        .map(this::formatComponent)
                        .collect(Collectors.joining(",", "[", "]"));
    }

    private String formatComponent(Double value) {
        if (value == null) {
            return "0";
        }
        // halfvec accepts float4-compatible input; clamp to a readable precision.
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }
}
