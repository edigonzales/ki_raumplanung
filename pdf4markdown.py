from pathlib import Path
from docling.document_converter import DocumentConverter

INPUT_DIR = Path("./data/VP_OP/")
OUTPUT_DIR = Path("/Users/stefan/tmp/out_markdown")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

converter = DocumentConverter()

for pdf_path in sorted(INPUT_DIR.glob("*.pdf")):
    print(f"Converting {pdf_path.name} ...")
    result = converter.convert(pdf_path)
    doc = result.document

    md = doc.export_to_markdown()

    out_path = OUTPUT_DIR / f"{pdf_path.stem}.md"
    out_path.write_text(md, encoding="utf-8")

    print(f"  -> wrote {out_path}")