package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HybridSearchServiceSqlTest {

    @Test
    void exposesHybridSearchSqlForPostgresSchema() {
        String sql = HybridSearchSql.HYBRID_SEARCH_SQL;

        assertThat(sql)
                .contains("arp_rag_vp.chunks")
                .contains("websearch_to_tsquery")
                .contains("embedding")
                .contains("halfvec");
    }
}
