@param java.util.List<ch.so.arp.rag.chat.DocumentChunk> results
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>RAG Assistent</title>
    <script src="https://unpkg.com/htmx.org@2.0.3"></script>
    <script src="https://unpkg.com/htmx.org@2.0.3/dist/ext/sse.js"></script>
    <style>
        .layout { display: flex; gap: 1rem; align-items: flex-start; }
        .column { flex: 1; }
        .result-card { border: 1px solid #000; padding: 0.5rem; margin-bottom: 0.5rem; }
    </style>
</head>
<body>
<h1>RAG Assistent</h1>
<div class="layout">
    <div class="column">
        <h2>Schritt 1: Suche</h2>
        <form hx-post="/search" hx-target="#search-results" hx-swap="innerHTML">
            <label for="keywords">Stichworte</label><br>
            <input type="text" id="keywords" name="keywords" required><br>
            <label for="lexicalWeight">Lexikalisches Gewicht (0 = semantisch, 1 = lexikalisch):</label><br>
            <input type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.5">
            <span id="lexicalWeightValue">0.5</span><br>
            <button type="submit">Suchen</button>
        </form>
        <div id="search-results">
            <p>Keine Suche ausgeführt.</p>
        </div>
    </div>
    <div class="column">
        <h2>Schritt 2: Kontext wählen und Aufgabe stellen</h2>
        <form hx-post="/summary" hx-target="#summary-panel" hx-swap="innerHTML"
              hx-include="#search-results input[type=checkbox]:checked">
            <label for="prompt">Aufgabe</label><br>
            <textarea id="prompt" name="prompt" rows="4" cols="50" required>Fasse die ausgewählten Abschnitte zusammen.</textarea><br>
            <button type="submit">Antwort generieren</button>
        </form>
        <div id="summary-panel">
            <p>Wähle zunächst Abschnitte aus der Suche.</p>
        </div>
    </div>
</div>
<script>
    const lexicalWeight = document.getElementById('lexicalWeight');
    const lexicalWeightValue = document.getElementById('lexicalWeightValue');

    if (lexicalWeight && lexicalWeightValue) {
        const updateLexicalLabel = (value) => {
            lexicalWeightValue.textContent = Number(value).toFixed(2);
        };

        lexicalWeight.addEventListener('input', (event) => {
            updateLexicalLabel(event.target.value);
        });

        updateLexicalLabel(lexicalWeight.value);
    }

    const searchResults = document.getElementById('search-results');
    if (searchResults) {
        searchResults.addEventListener('click', (event) => {
            const target = event.target;
            if (target && target.matches('[data-select-all]')) {
                const checkboxes = searchResults.querySelectorAll('input[type="checkbox"]');
                checkboxes.forEach((checkbox) => {
                    checkbox.checked = true;
                });
            }
        });
    }
</script>
</body>
</html>
