#!/usr/bin/env python3
"""
Python ingestion script mirroring ingest_data.java but using Docling as the PDF parser.
"""

import argparse
import hashlib
import json
import os
import re
import sys
import uuid
from pathlib import Path
from typing import Iterable, List, Optional, Sequence, Tuple

import requests
import tiktoken
from docling.document_converter import DocumentConverter
import psycopg2

# --- CONFIG ---
DB_URL = "postgresql://gretl:gretl@localhost:54323/arp_rag"
SCHEMA = "arp_rag_vp"
OPENAI_EMBEDDING_MODEL = "text-embedding-3-large"  # 3072 dims
EMBEDDING_DIMS = 3072

CHUNK_TOKENS = 450
CHUNK_OVERLAP = 90

SOBAU_REGEX = r"(?:SOBAU|Dossier)\s*(?:Nr\.\s*)?#\s*(\d{2,3}['\u2019\s]?\d{3})"
SOBAU_PAT = re.compile(SOBAU_REGEX)
FILENAME_PAT = re.compile(
    r"VP_(OP|GP)_([\w\p{L}\p{M}'\-\s]+?)(?:_|\.)",
    re.UNICODE,
)

HTTP = requests.Session()
ENCODING = tiktoken.encoding_for_model(OPENAI_EMBEDDING_MODEL)
DOC_CONVERTER = DocumentConverter()


class Section:
    def __init__(self, heading: Optional[str], text: str, page_from: Optional[int], page_to: Optional[int]):
        self.heading = heading
        self.text = text
        self.page_from = page_from
        self.page_to = page_to


class Meta:
    def __init__(self, municipality: str, plan_type: str):
        self.municipality = municipality
        self.plan_type = plan_type


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Ingest PDFs into Postgres using Docling for parsing",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument("inputs", nargs="+", help="PDF file(s) or directories containing PDFs")
    parser.add_argument("--no-openai", action="store_true", help="Use deterministic dummy embeddings")
    parser.add_argument("--reset", action="store_true", help="Truncate target schema before ingest")
    parser.add_argument("--run-dry", action="store_true", help="Dry run: parse and chunk without DB or embeddings")
    return parser.parse_args()


def parse_filename(filename: str) -> Meta:
    m = FILENAME_PAT.search(filename)
    plan = None
    muni = None
    if m:
        plan_code = m.group(1).upper()
        plan = "ortsplanung" if plan_code == "OP" else "gestaltungsplan" if plan_code == "GP" else None
        muni = m.group(2).replace("_", " ").strip()
    if plan is None:
        plan = "ortsplanung"
    if muni is None:
        muni = "Unbekannt"
    return Meta(muni, plan)


def sha256(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def dummy_embed(text: str) -> List[float]:
    seed = hashlib.sha256(text.encode("utf-8")).digest()
    value = int.from_bytes(seed[:8], byteorder="big", signed=False)
    rng = (value * 6364136223846793005 + 1) % (2**64)
    vec = []
    for _ in range(EMBEDDING_DIMS):
        rng = (rng * 6364136223846793005 + 1) % (2**64)
        val = ((rng >> 32) / float(2**32 - 1)) * 2 - 1
        vec.append(val)
    norm = sum(v * v for v in vec) ** 0.5
    if norm:
        vec = [float(v / norm) for v in vec]
    return vec


def to_pgvector(values: Sequence[float]) -> str:
    return f"[{','.join(f'{v}' for v in values)}]"


def openai_embed_batch(chunks: List[str]) -> List[List[float]]:
    api_key = os.getenv("OPENAI_API_KEY")
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }
    payload = {"model": OPENAI_EMBEDDING_MODEL, "input": chunks}
    resp = HTTP.post("https://api.openai.com/v1/embeddings", headers=headers, data=json.dumps(payload))
    if resp.status_code >= 300:
        raise RuntimeError(f"OpenAI Embeddings HTTP {resp.status_code}: {resp.text}")
    data = resp.json()
    embeddings = []
    for item in data.get("data", []):
        embeddings.append(item.get("embedding", []))
    if len(embeddings) != len(chunks):
        raise RuntimeError("Embedding count mismatch")
    return embeddings


def chunk_text_by_tokens(text: str, max_tokens: int, overlap: int) -> List[str]:
    tokens = ENCODING.encode(text)
    chunks: List[str] = []
    start = 0
    while start < len(tokens):
        end = min(start + max_tokens, len(tokens))
        chunk_tokens = tokens[start:end]
        chunks.append(ENCODING.decode(chunk_tokens))
        if end == len(tokens):
            break
        start = end - overlap
        if start < 0:
            start = 0
    return chunks


def infer_topics(chunk: str) -> List[str]:
    lc = chunk.lower()
    topics: List[str] = []
    if "hecke" in lc:
        topics.append("Hecken")
    if "fruchtfolgefläch" in lc or "fff" in lc:
        topics.append("Fruchtfolgeflächen")
    if "gewässerraum" in lc:
        topics.append("Gewässerraum")
    if "lärm" in lc:
        topics.append("Lärm")
    if "waldabstand" in lc:
        topics.append("Waldabstand")
    if "baulinie" in lc:
        topics.append("Baulinien")
    if "energiegründach" in lc:
        topics.append("Energiegründach")
    if "sichtzonen" in lc:
        topics.append("Sichtzonen")
    if "siedlungsränder" in lc:
        topics.append("Siedlungsränder")
    if "biber" in lc:
        topics.append("Biber")
    if "isos" in lc:
        topics.append("ISOS")
    if "bauverpflichtung" in lc:
        topics.append("Bauverpflichtung")
    return topics


def convert_pdf(pdf_path: Path) -> Tuple[str, Optional[int]]:
    result = DOC_CONVERTER.convert(pdf_path)
    document = result.document
    markdown = document.export_to_markdown()
    page_count = None
    if hasattr(document, "pages"):
        try:
            page_count = len(document.pages)
        except Exception:
            page_count = None
    if page_count is None:
        try:
            import pypdf

            with pdf_path.open("rb") as fh:
                reader = pypdf.PdfReader(fh)
                page_count = len(reader.pages)
        except Exception:
            page_count = None
    return markdown, page_count


def split_sections(markdown: str) -> List[Section]:
    sections: List[Section] = []
    current_heading: Optional[str] = None
    buffer: List[str] = []

    for line in markdown.splitlines():
        m = re.match(r"^(#+)\s+(.*)$", line)
        if m:
            if buffer or current_heading is not None:
                text = "\n".join(buffer).strip()
                if text or current_heading:
                    sections.append(Section(current_heading, text, None, None))
                buffer = []
            current_heading = m.group(2).strip()
        else:
            buffer.append(line)

    if buffer or current_heading is not None:
        text = "\n".join(buffer).strip()
        if text or current_heading:
            sections.append(Section(current_heading, text, None, None))

    if not sections:
        sections.append(Section(None, markdown.strip(), None, None))
    return sections


def guess_title(markdown: str) -> Optional[str]:
    for line in markdown.splitlines():
        t = line.strip()
        if len(t) > 3:
            return t if len(t) <= 200 else t[:200]
    return None


def reset_database(conn) -> None:
    with conn.cursor() as cur:
        print(">> RESET: Schema arp_rag_vp wird geleert …")
        cur.execute(
            f"""
            TRUNCATE {SCHEMA}.chunks,
                     {SCHEMA}.sobau_refs,
                     {SCHEMA}.sections,
                     {SCHEMA}.documents
            RESTART IDENTITY CASCADE
            """
        )
    conn.commit()
    print(">> RESET: fertig.")


def insert_sobau(conn, doc_id, sobau_code: str, raw: str, page: Optional[int]):
    with conn.cursor() as cur:
        cur.execute(
            f"""
            INSERT INTO {SCHEMA}.sobau_refs (document_id, sobau_code, raw, page)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (document_id, sobau_code) DO NOTHING
            """,
            (doc_id, sobau_code, raw, page),
        )


def ingest_pdf(conn, pdf_path: Path, run_dry: bool, no_openai: bool) -> None:
    print(f">> Ingest: {pdf_path}")
    markdown, page_count = convert_pdf(pdf_path)
    meta = parse_filename(pdf_path.name)

    document_id = uuid.uuid4()

    if conn:
        with conn.cursor() as cur:
            cur.execute(
                f"""
                INSERT INTO {SCHEMA}.documents (id, filename, title, plan_type, municipality, pages, source_url)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                """,
                (
                    document_id,
                    pdf_path.name,
                    guess_title(markdown),
                    meta.plan_type,
                    meta.municipality,
                    page_count,
                    str(pdf_path.resolve()),
                ),
            )
    elif run_dry:
        print(f"Dokument: {pdf_path.name} ({meta.plan_type}, {meta.municipality}, {page_count or 'n/a'} Seiten)")

    for m in SOBAU_PAT.finditer(markdown):
        number_like = m.group(1)
        digits_only = re.sub(r"['\u2019]", "", number_like)
        sobau_code = str(int(digits_only))
        if conn:
            insert_sobau(conn, document_id, sobau_code, number_like, None)
        else:
            print(f"  SOBAU gefunden: {sobau_code}")

    sections = split_sections(markdown)
    if run_dry:
        print(f"Erkannte Sektionen: {len(sections)}")

    for idx, section in enumerate(sections, start=1):
        section_id = None
        if conn:
            with conn.cursor() as cur:
                cur.execute(
                    f"""
                    INSERT INTO {SCHEMA}.sections (document_id, section_path, page_from, page_to)
                    VALUES (%s, %s, %s, %s)
                    RETURNING id
                    """,
                    (document_id, section.heading, section.page_from, section.page_to),
                )
                res = cur.fetchone()
                section_id = res[0] if res else None
        elif run_dry:
            heading = section.heading if section.heading else "[ohne Überschrift]"
            print(f"== Abschnitt {idx}: {heading} (Seiten {section.page_from or '?'}-{section.page_to or '?'})")

        chunks = chunk_text_by_tokens(section.text, CHUNK_TOKENS, CHUNK_OVERLAP)
        if not chunks:
            continue

        metas = []
        char_cursor = 0
        for chunk in chunks:
            start_char = char_cursor
            end_char = start_char + len(chunk)
            char_cursor = end_char
            digest = sha256(
                f"{meta.municipality}|{meta.plan_type}|{pdf_path}|{section.page_from}-{section.page_to}|{section.heading or ''}|{chunk}"
            )
            local_sobau = sorted({m.group(1) for m in SOBAU_PAT.finditer(chunk)})
            metas.append(
                {
                    "chunk": chunk,
                    "start": start_char,
                    "end": end_char,
                    "digest": digest,
                    "topics": infer_topics(chunk),
                    "sobau": local_sobau,
                }
            )

        if run_dry:
            for c_idx, meta_local in enumerate(metas, start=1):
                print(
                    f"  Chunk {idx}.{c_idx} (Tokens={ENCODING.count_tokens(meta_local['chunk'])}, Zeichen={len(meta_local['chunk'])})"
                )
                print(meta_local["chunk"])
                print("  ----")
            continue

        vectors: List[List[float]] = []
        if no_openai:
            vectors = [dummy_embed(m["chunk"]) for m in metas]
        else:
            vectors = openai_embed_batch([m["chunk"] for m in metas])

        with conn.cursor() as cur:
            for m, emb in zip(metas, vectors):
                cur.execute(
                    f"""
                    INSERT INTO {SCHEMA}.chunks
                      (document_id, section_id, page_from, page_to, char_start, char_end, text, tsv,
                       embedding, municipality, plan_type, topics, sobau_codes, digest)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, to_tsvector('german', public.unaccent(regexp_replace(lower(coalesce(%s,'')), '\\s+', ' ', 'g'))),
                            %s::vector, %s, %s, %s, %s, %s)
                    ON CONFLICT (digest) DO NOTHING
                    """,
                    (
                        document_id,
                        section_id,
                        section.page_from,
                        section.page_to,
                        m["start"],
                        m["end"],
                        m["chunk"],
                        m["chunk"],
                        to_pgvector(emb),
                        meta.municipality,
                        meta.plan_type,
                        m["topics"],
                        m["sobau"],
                        m["digest"],
                    ),
                )
    if conn:
        conn.commit()


def iter_pdfs(inputs: Iterable[str]) -> Iterable[Path]:
    for raw in inputs:
        path = Path(raw)
        if path.is_dir():
            for pdf in sorted(path.rglob("*.pdf")):
                yield pdf
        elif path.is_file() and path.suffix.lower() == ".pdf":
            yield path
        else:
            print(f"Übersprungen (kein PDF): {path}")


def main() -> None:
    args = parse_args()
    no_openai = args.no_openai
    run_dry = args.run_dry

    if run_dry:
        no_openai = True

    if not no_openai and os.getenv("OPENAI_API_KEY") is None:
        sys.stderr.write("Hinweis: Kein OPENAI_API_KEY gesetzt. Nutze --no-openai oder exportiere OPENAI_API_KEY.\n")
        sys.exit(1)

    conn = None
    if not run_dry:
        conn = psycopg2.connect(DB_URL)
        conn.autocommit = False

    try:
        if args.reset and conn:
            reset_database(conn)
            if not args.inputs:
                print("Reset ausgeführt. (Keine Dateien angegeben, Ende.)")
                return
            else:
                print("Reset ausgeführt. Fahre mit Ingest fort …")

        for pdf in iter_pdfs(args.inputs):
            try:
                ingest_pdf(conn, pdf, run_dry, no_openai)
            except Exception as exc:
                sys.stderr.write(f"Fehler bei: {pdf} -> {exc}\n")
                raise

        if conn:
            conn.commit()
        print("Run-Dry abgeschlossen." if run_dry else "Ingest abgeschlossen.")
    finally:
        if conn:
            conn.close()


if __name__ == "__main__":
    main()
