-- Create read-only role and grant privileges
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'gretl_ro') THEN
    CREATE ROLE gretl_ro LOGIN PASSWORD 'change_me_ro' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
  END IF;
END$$;

GRANT CONNECT ON DATABASE arp_rag TO gretl_ro;
GRANT USAGE ON SCHEMA public TO gretl_ro; -- needed for pgvector extension objects
GRANT USAGE ON SCHEMA arp_rag_vp    TO gretl_ro;

GRANT SELECT ON ALL TABLES    IN SCHEMA arp_rag_vp TO gretl_ro;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA arp_rag_vp TO gretl_ro;

ALTER DEFAULT PRIVILEGES IN SCHEMA arp_rag_vp GRANT SELECT ON TABLES    TO gretl_ro;
ALTER DEFAULT PRIVILEGES IN SCHEMA arp_rag_vp GRANT SELECT ON SEQUENCES TO gretl_ro;

ALTER ROLE gretl    IN DATABASE arp_rag SET search_path = arp_rag_vp, public;
ALTER ROLE gretl_ro IN DATABASE arp_rag SET search_path = arp_rag_vp, public;

-- Tighten PUBLIC (optional)
REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA arp_rag_vp FROM PUBLIC;
REVOKE USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA arp_rag_vp FROM PUBLIC;
