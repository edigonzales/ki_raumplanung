package ch.so.arp.rag.chat;

final class HybridSearchSql {

    private HybridSearchSql() {
    }

    static final String HYBRID_SEARCH_SQL = """
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
}
