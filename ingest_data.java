///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21
//DEPS org.apache.pdfbox:pdfbox:3.0.3
//DEPS org.postgresql:postgresql:42.7.3
//DEPS com.knuddels:jtokkit:1.1.0
//DEPS org.slf4j:slf4j-simple:2.0.13

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import org.apache.pdfbox.Loader;                  // <— neu
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.knuddels.jtokkit.api.*;
import com.knuddels.jtokkit.*;
import com.knuddels.jtokkit.api.ModelType;

public class ingest_data {                         // <— Klassenname == Dateiname

    // --- DB CONFIG ---
    static final String DB_URL  = "jdbc:postgresql://localhost:54323/arp_rag";
    static final String DB_USER = "gretl";
    static final String DB_PWD  = "gretl";

    // --- RAG PARAMS ---
    static final String SCHEMA = "arp_rag_vp";
    static final String OPENAI_EMBEDDING_MODEL = "text-embedding-3-large"; // 3072 dims
    static final int EMBEDDING_DIMS = 3072;

    // Chunking
    static final int CHUNK_TOKENS = 450;
    static final int CHUNK_OVERLAP = 90;

    // Regex
    static final String regex = "(?:SOBAU|Dossier)\\s*(?:Nr\\.\\s*)?#\\s*(\\d{2,3}['\\u2019\\s]?\\d{3})";
    static final Pattern SOBAU_PAT  = Pattern.compile(regex);
    static final Pattern FILENAME_PAT = Pattern.compile(
        "VP_(OP|GP)_([\\p{L}\\p{M}'\\-\\s]+?)(?:_|\\.)",
        Pattern.UNICODE_CHARACTER_CLASS
    );

    // Tokenizer
    static final EncodingRegistry REGISTRY = Encodings.newDefaultEncodingRegistry();
    static final Encoding ENCODING = REGISTRY.getEncodingForModel(ModelType.TEXT_EMBEDDING_3_LARGE);

    // CLI Flags
    static boolean NO_OPENAI = false;
    static boolean RESET_ONLY = false;

    // minimaler HTTP-Client für Embeddings
    static final HttpClient HTTP = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        List<String> inputs = new ArrayList<>();
        for (String a : args) {
            switch (a) {
                case "--help", "-h" -> { printHelp(); return; }
                case "--no-openai" -> NO_OPENAI = true;
                case "--reset" -> RESET_ONLY = true;
                default -> inputs.add(a);
            }
        }

        if (!NO_OPENAI && System.getenv("OPENAI_API_KEY") == null) {
            System.err.println("Hinweis: Kein OPENAI_API_KEY gesetzt. Nutze --no-openai oder exportiere OPENAI_API_KEY.");
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD)) {
            conn.setAutoCommit(false);

            if (RESET_ONLY) {
                resetDatabase(conn);
                conn.commit();
                if (inputs.isEmpty()) {
                    System.out.println("Reset ausgeführt. (Keine Dateien angegeben, Ende.)");
                    return;
                } else {
                    System.out.println("Reset ausgeführt. Fahre mit Ingest fort …");
                }
            }

            if (inputs.isEmpty()) {
                System.err.println("Keine Eingaben. Nutze: jbang ingest_data.java [--no-openai] [--reset] <pdf-or-folder> …");
                return;
            }

            for (String arg : inputs) {
                Path p = Paths.get(arg);
                if (Files.isDirectory(p)) {
                    try (Stream<Path> s = Files.walk(p)) {
                        s.filter(f -> f.toString().toLowerCase().endsWith(".pdf")).forEach(pdf -> {
                            try { ingestPdf(conn, pdf); } catch (Exception e) { logErr(pdf, e); }
                        });
                    }
                } else if (Files.isRegularFile(p) && p.toString().toLowerCase().endsWith(".pdf")) {
                    ingestPdf(conn, p);
                } else {
                    System.err.println("Übersprungen (kein PDF): " + p);
                }
            }
            conn.commit();
            System.out.println("Ingest abgeschlossen.");
        }
    }

    static void printHelp() {
        System.out.println("""
        Nutzung: jbang ingest_data.java [Optionen] <pdf-or-folder> [mehrere …]

        Optionen:
          --no-openai   : Dummy-Embeddings (deterministisch). Kein OPENAI_API_KEY nötig.
          --reset       : Leert Schema arp_rag_vp (TRUNCATE … CASCADE). Mit Pfaden: danach ingest.
          -h, --help    : Hilfe anzeigen.
        """);
    }

    static void resetDatabase(Connection conn) throws SQLException {
        System.out.println(">> RESET: Schema arp_rag_vp wird geleert …");
        try (Statement st = conn.createStatement()) {
            st.execute("""
                TRUNCATE arp_rag_vp.chunks,
                         arp_rag_vp.sobau_refs,
                         arp_rag_vp.sections,
                         arp_rag_vp.documents
                RESTART IDENTITY CASCADE
            """);
        }
        System.out.println(">> RESET: fertig.");
    }

    static void ingestPdf(Connection conn, Path pdf) throws Exception {
        System.out.println(">> Ingest: " + pdf);
        // PDF laden (PDFBox 3.x)
        String text;
        int pages;
        try (PDDocument doc = Loader.loadPDF(Files.readAllBytes(pdf))) {   // <— Loader.loadPDF
            pages = doc.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(doc);
        }

        var meta = parseFilename(pdf.getFileName().toString());
        String planType = meta.planType;
        String municipality = meta.municipality;

        UUID docId = UUID.randomUUID();
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO arp_rag_vp.documents (id, filename, title, plan_type, municipality, pages, source_url)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """)) {
            ps.setObject(1, docId);
            ps.setString(2, pdf.getFileName().toString());
            ps.setString(3, stripPdfTitleGuess(text));
            ps.setString(4, planType);
            ps.setString(5, municipality);
            ps.setInt(6, pages);
            ps.setString(7, pdf.toAbsolutePath().toString());
            ps.executeUpdate();
        }

        // globale SOBAU-Erfassung (optional Seite unbekannt)
        Matcher mm = SOBAU_PAT.matcher(text);
        while (mm.find()) {
            String numberLike = mm.group(1);                      
            String digitsOnly  = numberLike.replaceAll("['\\u2019]", ""); // Apostroph/’ entfernen
            int value = Integer.parseInt(digitsOnly);  
            System.err.println("SOBAU: " + value);       
            insertSobau(conn, docId, String.valueOf(value), String.valueOf(value), null);
        }

        // Seitenweise chunking
        List<PageBlock> pageBlocks = splitByPages(pdf);
        for (PageBlock pb : pageBlocks) {
            List<String> chunks = chunkTextByTokens(pb.text, CHUNK_TOKENS, CHUNK_OVERLAP);
            int charCursor = 0;
            for (String chunk : chunks) {
                int start = charCursor, end = start + chunk.length();
                charCursor = end;

                String digest = sha256(municipality + "|" + planType + "|" + pdf + "|" + pb.page + "|" + chunk);
                float[] emb = NO_OPENAI ? dummyEmbed(chunk) : openaiEmbed(chunk);

                // lokale SOBAU-Codes im Chunk
                Set<String> localSobau = new LinkedHashSet<>();
                Matcher sm = SOBAU_PAT.matcher(chunk);
                while (sm.find()) localSobau.add(sm.group(1));
                //System.err.println("localSobau: " + localSobau);
                Array sobauArray = conn.createArrayOf("text", localSobau.toArray(new String[0]));

                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO arp_rag_vp.chunks
                          (document_id, page_from, page_to, char_start, char_end, text, tsv,
                           embedding, municipality, plan_type, topics, sobau_codes, digest)
                        VALUES (?, ?, ?, ?, ?, ?, to_tsvector('german', public.unaccent(regexp_replace(lower(coalesce(?, '')), '\s+', ' ', 'g'))),
                                ?::vector, ?, ?, ?, ?, ?)
                        ON CONFLICT (digest) DO NOTHING
                    """)) {
                    ps.setObject(1, docId);
                    ps.setInt(2, pb.page);
                    ps.setInt(3, pb.page);
                    ps.setInt(4, start);
                    ps.setInt(5, end);
                    ps.setString(6, chunk);
                    ps.setString(7, chunk);
                    ps.setString(8, toPgVector(emb));
                    ps.setString(9, municipality);
                    ps.setString(10, planType);
                    ps.setArray(11, conn.createArrayOf("text", inferTopics(chunk)));
                    ps.setArray(12, sobauArray);
                    ps.setString(13, digest);
                    ps.executeUpdate();
                }
            }
        }
        conn.commit();
    }

    // --- Helpers ---
    static record Meta(String municipality, String planType) {}
    static Meta parseFilename(String filename) {
        Matcher m = FILENAME_PAT.matcher(filename);
        String plan = null, muni = null;
        if (m.find()) {
            System.err.println(m.toString());
            System.err.println(m.group(1));
            plan = switch (m.group(1).toUpperCase()) {
                case "OP" -> "ortsplanung";
                case "GP" -> "gestaltungsplan";
                default -> null;
            };
            muni = m.group(2).replace('_',' ').trim();
            System.err.println(muni);
        }
        if (plan == null) plan = "ortsplanung";
        if (muni == null) muni = "Unbekannt";
        return new Meta(muni, plan);
    }

    static String stripPdfTitleGuess(String text) {
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (!t.isEmpty() && t.length() > 3) return t.length() > 200 ? t.substring(0,200) : t;
        }
        return null;
    }

    static String[] inferTopics(String chunk) {
        String lc = chunk.toLowerCase(Locale.ROOT);
        List<String> topics = new ArrayList<>();
        if (lc.contains("hecke")) topics.add("Hecken");
        if (lc.contains("fruchtfolgefläch") || lc.contains("fff")) topics.add("Fruchtfolgeflächen");
        if (lc.contains("gewässerraum")) topics.add("Gewässerraum");
        if (lc.contains("lärm")) topics.add("Lärm");
        if (lc.contains("waldabstand")) topics.add("Waldabstand");
        if (lc.contains("baulinie")) topics.add("Baulinien");
        if (lc.contains("energiegründach")) topics.add("Energiegründach");
        if (lc.contains("sichtzonen")) topics.add("Sichtzonen");
        if (lc.contains("siedlungsränder")) topics.add("Siedlungsränder");
        if (lc.contains("biber")) topics.add("Biber");
        if (lc.contains("isos")) topics.add("ISOS");
        if (lc.contains("bauverpflichtung")) topics.add("Bauverpflichtung");
        return topics.toArray(new String[0]);
    }

    static class PageBlock { final int page; final String text; PageBlock(int p, String t){page=p;text=t;} }
    static List<PageBlock> splitByPages(Path pdf) throws IOException {
        List<PageBlock> out = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(Files.readAllBytes(pdf))) {  // <— Loader.loadPDF
            int n = doc.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            for (int p=1; p<=n; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String t = stripper.getText(doc);
                t = t.replace("\u00A0", " ").replaceAll("[ \\t\\x0B\\f\\r]+", " ");
                out.add(new PageBlock(p, t));
            }
        }
        return out;
    }

    static List<String> chunkTextByTokens(String text, int chunkTokens, int overlapTokens) {
        List<String> paras = Arrays.stream(text.split("\\R{2,}"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        List<String> chunks = new ArrayList<>();
        List<String> buffer = new ArrayList<>();
        int tokCount = 0;

        for (String para : paras) {
            int ptoks = ENCODING.countTokens(para);
            if (ptoks > chunkTokens) {
                chunks.addAll(splitLongParagraph(para, chunkTokens, overlapTokens));
                buffer.clear();
                tokCount = 0;
                continue;
            }
            if (tokCount + ptoks > chunkTokens && !buffer.isEmpty()) {
                chunks.add(String.join("\n\n", buffer));
                buffer = keepTailByTokens(buffer, overlapTokens);
                tokCount = tokenCount(buffer);
            }
            buffer.add(para);
            tokCount += ptoks;
        }
        if (!buffer.isEmpty()) chunks.add(String.join("\n\n", buffer));
        return chunks;
    }

    static List<String> splitLongParagraph(String text, int chunkTokens, int overlapTokens) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < words.length) {
            int end = Math.min(words.length, start + Math.max(1,(int)Math.round(chunkTokens*0.75)));
            String chunk = String.join(" ", Arrays.copyOfRange(words, start, end));
            int tokens = ENCODING.countTokens(chunk);
            while (tokens > chunkTokens && end > start + 10) {
                end -= 10;
                chunk = String.join(" ", Arrays.copyOfRange(words, start, end));
                tokens = ENCODING.countTokens(chunk);
            }
            chunks.add(chunk);
            int back = Math.max(0, (int)Math.round(overlapTokens*0.75));
            start = Math.max(start + (end - start) - back, start + 1);
            if (start >= end) start = end;
        }
        return chunks;
    }

    static List<String> keepTailByTokens(List<String> paras, int tailTokens) {
        List<String> out = new ArrayList<>();
        int tok = 0;
        for (int i = paras.size()-1; i >= 0; i--) {
            String p = paras.get(i);
            int pt = ENCODING.countTokens(p);
            if (tok + pt > tailTokens && !out.isEmpty()) break;
            out.add(0, p);
            tok += pt;
            if (tok >= tailTokens) break;
        }
        return out;
    }
    static int tokenCount(List<String> paras){ return paras.stream().mapToInt(ENCODING::countTokens).sum(); }

    static void insertSobau(Connection conn, UUID docId, String norm, String raw, Integer page) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO arp_rag_vp.sobau_refs (document_id, sobau_code, raw, page)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (document_id, sobau_code) DO NOTHING
        """)) {
            ps.setObject(1, docId);
            ps.setString(2, norm);
            ps.setString(3, raw);
            if (page == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, page);
            ps.executeUpdate();
        }
    }

    // --- Embeddings ---

    static float[] openaiEmbed(String text) throws IOException, InterruptedException {
        // Direkter REST-Call zur OpenAI Embeddings API (stabil laut API-Referenz)
        String apiKey = System.getenv("OPENAI_API_KEY");
        String body = """
        {"model":"%s","input":%s}
        """.formatted(OPENAI_EMBEDDING_MODEL, jsonEscape(text));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<InputStream> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() >= 300) {
            String err = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("OpenAI Embeddings HTTP " + resp.statusCode() + ": " + err);
        }
        // kleine, einfache JSON-Parsing-Routine ohne extra Dependency
        // wir suchen das erste "embedding":[ ... ] und lesen die Floats heraus
        String json = new String(resp.body().readAllBytes(), StandardCharsets.UTF_8);
        int idx = json.indexOf("\"embedding\"");
        if (idx < 0) throw new IOException("Kein 'embedding' Feld in Antwort");
        int start = json.indexOf('[', idx);
        int end = json.indexOf(']', start);
        String arr = json.substring(start+1, end);
        String[] parts = arr.split(",");
        float[] v = new float[parts.length];
        for (int i=0; i<parts.length; i++) v[i] = Float.parseFloat(parts[i].trim());
        return v;
    }

    static String jsonEscape(String s){
        String esc = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n","\\n").replace("\r","\\r");
        return "\"" + esc + "\"";
    }

    static float[] dummyEmbed(String text) {
        byte[] seed = sha256Bytes(text);
        Random r = new Random(bytesToLong(seed));
        float[] v = new float[EMBEDDING_DIMS];
        for (int i=0; i<EMBEDDING_DIMS; i++) v[i] = (r.nextFloat() * 2f) - 1f;
        double norm = 0.0; for (float f : v) norm += f*f; norm = Math.sqrt(norm);
        if (norm > 0) for (int i=0; i<v.length; i++) v[i] /= (float)norm;
        return v;
    }

    static String toPgVector(float[] emb) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i=0; i<emb.length; i++) { if (i>0) sb.append(','); sb.append(Float.toString(emb[i])); }
        sb.append(']');
        return sb.toString();
    }

    static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    static byte[] sha256Bytes(String s){ try { return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)); } catch(Exception e){throw new RuntimeException(e);} }
    static long bytesToLong(byte[] b){ long x=0L; for(int i=0;i<8&&i<b.length;i++){ x=(x<<8)|(b[i]&0xff);} return x; }

    static void logErr(Path pdf, Exception e) {
        System.err.println("Fehler bei: " + pdf + " -> " + e.getMessage());
        e.printStackTrace(System.err);
    }
}
