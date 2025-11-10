-- Erweiterungen
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS vector;  -- pgvector
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE SCHEMA IF NOT EXISTS arp_rag_vp;

-- Dokumente
CREATE TABLE arp_rag_vp.documents (
  id UUID PRIMARY KEY,
  filename TEXT NOT NULL,
  title TEXT,
  plan_type TEXT CHECK (plan_type IN ('ortsplanung','gestaltungsplan')),
  municipality TEXT,
  doc_date DATE,
  source_url TEXT,          -- Link zum PDF (z. B. MinIO/S3)
  pages INT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- SOBAU-Referenzen (n:1 je Dokument, aber auch in Chunks)
CREATE TABLE arp_rag_vp.sobau_refs (
  id BIGSERIAL PRIMARY KEY,
  document_id UUID REFERENCES arp_rag_vp.documents(id) ON DELETE CASCADE,
  sobau_code TEXT,          -- z. B. 82_326
  raw TEXT,                 -- Original "SOBAU #82'326"
  page INT,
  UNIQUE (document_id, sobau_code)
);

-- Sektionen (optional, wenn du strukturiert speichern willst)
CREATE TABLE arp_rag_vp.sections (
  id BIGSERIAL PRIMARY KEY,
  document_id UUID REFERENCES arp_rag_vp.documents(id) ON DELETE CASCADE,
  section_path TEXT,        -- "2 > Landschaft > Naturwerte > Hecken"
  page_from INT,
  page_to INT
);

-- Chunks (RAG-Kerneinheit)
CREATE TABLE arp_rag_vp.chunks (
  id BIGSERIAL PRIMARY KEY,
  document_id UUID REFERENCES arp_rag_vp.documents(id) ON DELETE CASCADE,
  section_id BIGINT REFERENCES arp_rag_vp.sections(id),
  page_from INT,
  page_to INT,
  char_start INT,
  char_end INT,
  text TEXT NOT NULL,
  tsv tsvector,             -- Volltext
  embedding halfvec(3072),   -- Dimension je nach Modell
  municipality TEXT,
  plan_type TEXT,
  topics TEXT[],            -- {'Hecken','FFF',...}
  sobau_codes TEXT[],       -- {'82_326', ...}
  digest TEXT
);

-- Indizes
CREATE INDEX ON arp_rag_vp.chunks USING GIN (tsv);
CREATE INDEX ON arp_rag_vp.chunks USING ivfflat (embedding halfvec_cosine_ops) WITH (lists = 200);
CREATE INDEX ON arp_rag_vp.chunks (municipality, plan_type);
CREATE INDEX ON arp_rag_vp.chunks USING GIN (topics);
CREATE UNIQUE INDEX IF NOT EXISTS chunks_digest_uniq ON arp_rag_vp.chunks(digest);
