package ch.so.arp.rag.chat;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * PostgreSQL backed {@link VectorDatabase} implementation. It expects a table
 * named {@code rag_context} that contains at least the columns {@code id},
 * {@code question} and {@code chunk}. The exact similarity search is not part of
 * this sample yet but the class showcases how the integration can be wired.
 */
class PostgresVectorDatabase implements VectorDatabase {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    PostgresVectorDatabase(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> findRelevantContext(String question) {
        String sql = """
                SELECT chunk
                FROM rag_context
                WHERE LOWER(question) LIKE LOWER(:pattern)
                ORDER BY id
                LIMIT 5
                """;
        Map<String, Object> params = Map.of("pattern", "%" + question + "%");
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("chunk"));
    }
}
