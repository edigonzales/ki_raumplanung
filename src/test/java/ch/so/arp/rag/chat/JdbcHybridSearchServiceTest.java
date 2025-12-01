package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class JdbcHybridSearchServiceTest {

    @Test
    void populatesEmbeddingLiteralWhenAvailable() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        QueryEmbeddingService embeddingService = mock(QueryEmbeddingService.class);
        when(embeddingService.embed("hello")).thenReturn(Optional.of(List.of(0.1, 0.2)));
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        JdbcHybridSearchService service = new JdbcHybridSearchService(jdbcTemplate, embeddingService);
        service.search("hello");

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(eq(HybridSearchSql.HYBRID_SEARCH_SQL), params.capture(), any(RowMapper.class));

        assertThat(params.getValue().getValue("embedding")).isEqualTo("[0.100000,0.200000]");
    }

    @Test
    void leavesEmbeddingNullWhenNotAvailable() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        QueryEmbeddingService embeddingService = mock(QueryEmbeddingService.class);
        when(embeddingService.embed("hello")).thenReturn(Optional.empty());
        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        JdbcHybridSearchService service = new JdbcHybridSearchService(jdbcTemplate, embeddingService);
        service.search("hello");

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate).query(eq(HybridSearchSql.HYBRID_SEARCH_SQL), params.capture(), any(RowMapper.class));

        assertThat(params.getValue().getValue("embedding")).isNull();
    }
}
