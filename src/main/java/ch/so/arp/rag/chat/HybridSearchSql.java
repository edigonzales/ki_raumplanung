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
                    GREATEST(LEAST(COALESCE(:lexical_weight, 0.6), 1.0), 0.0) AS lexical_weight,
                    1 - GREATEST(LEAST(COALESCE(:lexical_weight, 0.6), 1.0), 0.0) AS vector_weight,
                    COALESCE(:limit, 20)                           AS alimit
            ), ranked AS (
                SELECT
                    c.id,
                    c.document_id,
                    d.filename,
                    d.title,
                    c.section_id,
                    s.section_path,
                    string_agg(c.text, ' ' ORDER BY c.id) OVER (PARTITION BY COALESCE(c.section_id, c.id)) AS section_text,
                    substring(c.text FROM 1 FOR 240) AS snippet,
                    c.municipality,
                    c.plan_type,
                    ts_rank_cd(c.tsv, p.q)           AS keyword_score,
                    1 - (c.embedding <=> p.emb)      AS vector_score,
                    (p.lexical_weight * ts_rank_cd(c.tsv, p.q) + p.vector_weight * (1 - (c.embedding <=> p.emb))) AS hybrid_score
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
            SELECT id, document_id, filename, title, section_id, section_path, section_text, snippet,
                   municipality, plan_type, keyword_score, vector_score, hybrid_score
            FROM ranked
            ORDER BY hybrid_score DESC NULLS LAST, id ASC
            LIMIT (SELECT alimit FROM params);
            """;
}
