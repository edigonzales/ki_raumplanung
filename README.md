# ki_raumplanung

```
OPENAI_API_KEY=sk-proj-... jbang ingest_data.java  data/VP_Berichte
```

```
# Open a shell inside the Postgres container (optional helper)
docker compose exec pgvector bash

# Inside the container: dump data-only SQL with INSERT statements
pg_dump -U gretl --schema=arp_rag_vp --data-only --inserts arp_rag > /tmp/rag_inserts.sql

# Exit the container when done
exit

# Copy the dump file back to the host
docker compose cp pgvector:/tmp/rag_inserts.sql ./rag_inserts.sql
```