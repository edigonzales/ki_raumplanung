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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcHybridSearchServiceTest {

    @Test
    void populatesEmbeddingLiteralWhenAvailable() {
        NamedParameterJdbcOperations operations = mock(NamedParameterJdbcOperations.class);
        QueryEmbeddingService embeddingService = mock(QueryEmbeddingService.class);
        when(embeddingService.embed("hello")).thenReturn(Optional.of(List.of(0.1, 0.2)));
        when(operations.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        JdbcHybridSearchService service = new JdbcHybridSearchService(JdbcClient.create(operations), embeddingService);
        service.search("hello");

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(operations).query(eq(HybridSearchSql.HYBRID_SEARCH_SQL), params.capture(), any(RowMapper.class));

        assertThat(params.getValue().getValue("embedding")).isEqualTo("[0.100000,0.200000]");
    }

    @Test
    void normalizesCommaSeparatedKeywordsToOrQuery() {
        NamedParameterJdbcOperations operations = mock(NamedParameterJdbcOperations.class);
        QueryEmbeddingService embeddingService = mock(QueryEmbeddingService.class);
        when(embeddingService.embed("Hecke Baulinien")).thenReturn(Optional.empty());
        when(operations.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        JdbcHybridSearchService service = new JdbcHybridSearchService(JdbcClient.create(operations), embeddingService);
        service.search(" Hecke , Baulinien ");

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(operations).query(eq(HybridSearchSql.HYBRID_SEARCH_SQL), params.capture(), any(RowMapper.class));
        verify(embeddingService).embed("Hecke Baulinien");

        assertThat(params.getValue().getValue("keywords")).isEqualTo("Hecke OR Baulinien");
    }

    @Test
    void leavesEmbeddingNullWhenNotAvailable() {
        NamedParameterJdbcOperations operations = mock(NamedParameterJdbcOperations.class);
        QueryEmbeddingService embeddingService = mock(QueryEmbeddingService.class);
        when(embeddingService.embed("hello")).thenReturn(Optional.empty());
        when(operations.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        JdbcHybridSearchService service = new JdbcHybridSearchService(JdbcClient.create(operations), embeddingService);
        service.search("hello");

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(operations).query(eq(HybridSearchSql.HYBRID_SEARCH_SQL), params.capture(), any(RowMapper.class));

        assertThat(params.getValue().getValue("embedding")).isNull();
    }
}
