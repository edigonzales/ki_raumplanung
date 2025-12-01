package ch.so.arp.rag.chat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<DocumentChunk> search(String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return Collections.emptyList();
        }

        SqlParameterSource params = buildSearchParams(keywords);
        return jdbcClient.sql(HybridSearchSql.HYBRID_SEARCH_SQL)
                .paramSource(params)
                .query(DataClassRowMapper.newInstance(DocumentChunk.class))
                .list();
    }

    @Override
    public List<DocumentChunk> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = """
                SELECT c.id, c.document_id, d.filename, d.title, s.section_path,
                       substring(c.text FROM 1 FOR 240) AS snippet
                FROM arp_rag_vp.chunks c
                JOIN arp_rag_vp.documents d ON d.id = c.document_id
                LEFT JOIN arp_rag_vp.sections s ON s.id = c.section_id
                WHERE c.id IN (:ids)
                ORDER BY c.id ASC
                """;
        return jdbcClient.sql(sql)
                .param("ids", ids)
                .query(DataClassRowMapper.newInstance(DocumentChunk.class))
                .list();
    }

    @Override
    public String hybridSearchSql() {
        return HybridSearchSql.HYBRID_SEARCH_SQL;
    }

    MapSqlParameterSource buildSearchParams(String keywords) {
        return new MapSqlParameterSource()
                .addValue("keywords", keywords)
                .addValue("embedding", embeddingService.embed(keywords).map(this::toHalfvecLiteral).orElse(null))
                .addValue("municipality", null)
                .addValue("plan_type", null)
                .addValue("limit", 20);
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
