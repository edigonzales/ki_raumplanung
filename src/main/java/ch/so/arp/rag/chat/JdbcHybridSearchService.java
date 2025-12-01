package ch.so.arp.rag.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

public class JdbcHybridSearchService implements DocumentSearchService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcHybridSearchService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DocumentChunk> search(String keywords) {
        if (!StringUtils.hasText(keywords)) {
            return Collections.emptyList();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("keywords", keywords)
                .addValue("embedding", null)
                .addValue("municipality", null)
                .addValue("plan_type", null)
                .addValue("limit", 20);

        return jdbcTemplate.query(HybridSearchSql.HYBRID_SEARCH_SQL, params, documentChunkRowMapper());
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
        return jdbcTemplate.query(sql, Map.of("ids", ids), documentChunkRowMapper());
    }

    @Override
    public String hybridSearchSql() {
        return HybridSearchSql.HYBRID_SEARCH_SQL;
    }

    private RowMapper<DocumentChunk> documentChunkRowMapper() {
        return (ResultSet rs, int rowNum) -> new DocumentChunk(
                rs.getLong("id"),
                getUuid(rs, "document_id"),
                rs.getString("filename"),
                rs.getString("title"),
                rs.getString("section_path"),
                rs.getString("snippet"));
    }

    private UUID getUuid(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(String.valueOf(value));
    }
}
