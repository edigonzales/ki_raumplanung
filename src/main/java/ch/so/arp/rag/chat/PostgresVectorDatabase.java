package ch.so.arp.rag.chat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * PostgreSQL backed {@link VectorDatabase} implementation that performs hybrid
 * retrieval consisting of a lexical search, semantic similarity search,
 * reciprocal rank fusion and a final cross-encoder reranking pass.
 */
class PostgresVectorDatabase implements VectorDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresVectorDatabase.class);

    private static final String LEXICAL_SQL = """
            WITH params AS (
              SELECT lower(:query) AS qtext
            ),
            q_terms AS (
              SELECT DISTINCT unnest(tsvector_to_array(to_tsvector('german', (SELECT qtext FROM params)))) AS term
            ),
            q_filtered AS (
              SELECT term
              FROM q_terms
              WHERE length(term) >= 3
            ),
            q_or AS (
              SELECT to_tsquery('german', string_agg(quote_ident(term) || ':*', ' | ')) AS tsq
              FROM q_filtered
            ),
            q_must AS (
              SELECT COALESCE(
                       (SELECT tsq FROM q_or),
                       websearch_to_tsquery('german', (SELECT qtext FROM params))
                     ) AS tsq
            )
            SELECT
              c.id AS chunk_id,
              d.filename AS task_name,
              COALESCE(d.title, '') AS heading,
              COALESCE(d.source_url, '') AS url,
              COALESCE('Seite ' || c.page_from, '') AS anchor,
              COALESCE(c.text, '') AS content,
              ts_rank_cd(
                setweight(to_tsvector('german', COALESCE(d.title,'')), 'A') ||
                setweight(COALESCE(c.tsv, to_tsvector('german', '')), 'C'),
                (SELECT tsq FROM q_must),
                32
              ) AS lexical_score,
              0.0::double precision AS semantic_score
            FROM arp_rag_vp.chunks c
            JOIN arp_rag_vp.documents d ON c.document_id = d.id
            WHERE (SELECT tsq FROM q_must) @@ (
              setweight(to_tsvector('german', COALESCE(d.title,'')), 'A') ||
              setweight(COALESCE(c.tsv, to_tsvector('german', '')), 'C')
            )
            ORDER BY lexical_score DESC
            LIMIT :limit;
            """;

    private static final String SEMANTIC_SQL = """
            SELECT
              c.id AS chunk_id,
              d.filename AS task_name,
              COALESCE(d.title, '') AS heading,
              COALESCE(d.source_url, '') AS url,
              COALESCE('Seite ' || c.page_from, '') AS anchor,
              COALESCE(c.text, '') AS content,
              0.0::double precision AS lexical_score,
              (1.0 - (c.embedding <=> :embedding::vector)) AS semantic_score
            FROM arp_rag_vp.chunks c
            JOIN arp_rag_vp.documents d ON c.document_id = d.id
            WHERE c.embedding IS NOT NULL
            ORDER BY c.embedding <=> :embedding::vector
            LIMIT :limit;
            """;

    private static final int RRF_K = 60;

    private final JdbcClient jdbcClient;
    private final EmbeddingProvider embeddingProvider;
    private final CrossEncoderReranker reranker;

    PostgresVectorDatabase(JdbcClient jdbcClient, EmbeddingProvider embeddingProvider,
            CrossEncoderReranker reranker) {
        this.jdbcClient = Objects.requireNonNull(jdbcClient, "jdbcClient");
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider, "embeddingProvider");
        this.reranker = Objects.requireNonNull(reranker, "reranker");
    }

    @Override
    public List<RetrievedContext> findRelevantContext(String question, int limit) {
        int lexicalLimit = Math.max(limit * 3, limit);
        int semanticLimit = Math.max(limit * 6, limit);

        List<ChunkRow> lexicalResults = jdbcClient.sql(LEXICAL_SQL)
                .param("query", question)
                .param("limit", lexicalLimit)
                .query(ChunkRowMapper.INSTANCE)
                .list();

        float[] embedding = embeddingProvider.embed(question);
        List<ChunkRow> semanticResults = jdbcClient.sql(SEMANTIC_SQL)
                .param("embedding", toPgVectorLiteral(embedding))
                .param("limit", semanticLimit)
                .query(ChunkRowMapper.INSTANCE)
                .list();

        List<CandidateAccumulator> fused = reciprocalRankFusion(lexicalResults, semanticResults, limit * 3);

        Map<Long, Double> crossScores = reranker.score(question, fused.stream()
                .map(CandidateAccumulator::asContextCandidate).toList());
        fused.forEach(candidate -> candidate.setCrossEncoderScore(crossScores.getOrDefault(candidate.chunkId(), 0.0d)));

        LOGGER.debug("Hybrid retrieval produced {} fused candidates (limit={})", fused.size(), limit);

        return fused.stream()
                .sorted(Comparator.comparingDouble(CandidateAccumulator::crossEncoderScore).reversed()
                        .thenComparingDouble(CandidateAccumulator::fusedScore).reversed())
                .limit(limit)
                .map(CandidateAccumulator::toRetrievedContext)
                .toList();
    }

    private List<CandidateAccumulator> reciprocalRankFusion(List<ChunkRow> lexical, List<ChunkRow> semantic, int limit) {
        Map<Long, CandidateAccumulator> accumulator = new LinkedHashMap<>();
        applyFusion(lexical, accumulator, true);
        applyFusion(semantic, accumulator, false);
        return accumulator.values().stream()
                .sorted(Comparator.comparingDouble(CandidateAccumulator::fusedScore).reversed())
                .limit(limit)
                .toList();
    }

    private void applyFusion(List<ChunkRow> source, Map<Long, CandidateAccumulator> accumulator, boolean lexical) {
        int rank = 0;
        for (ChunkRow row : source) {
            rank++;
            CandidateAccumulator candidate = accumulator.computeIfAbsent(row.chunkId(), key -> new CandidateAccumulator(row));
            if (lexical) {
                candidate.setLexicalScore(row.lexicalScore());
            } else {
                candidate.setSemanticScore(row.semanticScore());
            }
            candidate.addToFusedScore(1.0d / (RRF_K + rank));
        }
    }

    private String toPgVectorLiteral(float[] embedding) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%f", embedding[i]));
        }
        builder.append(']');
        return builder.toString();
    }

    private enum ChunkRowMapper implements RowMapper<ChunkRow> {
        INSTANCE;

        @Override
        public ChunkRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ChunkRow(
                    rs.getLong("chunk_id"),
                    rs.getString("task_name"),
                    rs.getString("heading"),
                    rs.getString("url"),
                    rs.getString("anchor"),
                    rs.getString("content"),
                    rs.getDouble("lexical_score"),
                    rs.getDouble("semantic_score"));
        }
    }

    private record ChunkRow(long chunkId, String taskName, String heading, String url, String anchor, String content,
            double lexicalScore, double semanticScore) {
    }

    private static final class CandidateAccumulator {

        private final long chunkId;
        private final String taskName;
        private final String heading;
        private final String url;
        private final String anchor;
        private final String content;
        private double lexicalScore;
        private double semanticScore;
        private double fusedScore;
        private double crossEncoderScore;

        CandidateAccumulator(ChunkRow row) {
            this.chunkId = row.chunkId();
            this.taskName = row.taskName();
            this.heading = row.heading();
            this.url = row.url();
            this.anchor = row.anchor();
            this.content = row.content();
        }

        long chunkId() {
            return chunkId;
        }

        double fusedScore() {
            return fusedScore;
        }

        double crossEncoderScore() {
            return crossEncoderScore;
        }

        void setLexicalScore(double lexicalScore) {
            this.lexicalScore = lexicalScore;
        }

        void setSemanticScore(double semanticScore) {
            this.semanticScore = semanticScore;
        }

        void addToFusedScore(double increment) {
            fusedScore += increment;
        }

        void setCrossEncoderScore(double crossEncoderScore) {
            this.crossEncoderScore = crossEncoderScore;
        }

        CrossEncoderReranker.ContextCandidate asContextCandidate() {
            return new CrossEncoderReranker.ContextCandidate(chunkId, content, lexicalScore, semanticScore);
        }

        RetrievedContext toRetrievedContext() {
            return new RetrievedContext(chunkId, taskName, heading, url, anchor, content, lexicalScore, semanticScore,
                    fusedScore, crossEncoderScore);
        }
    }
}
