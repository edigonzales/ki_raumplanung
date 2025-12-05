@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>RAG Assistent</title>
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4/sse.min.js"></script>
    <style>
        .layout { display: flex; gap: 1rem; align-items: flex-start; }
        .column { flex: 1; }
        .result-card { border: 1px solid #000; padding: 0.5rem; margin-bottom: 0.5rem; }
        .document-header { display: flex; justify-content: space-between; gap: 0.5rem; align-items: flex-start; }
        .section-card { border: 1px solid #ddd; padding: 0.5rem; margin-top: 0.5rem; background: #fafafa; }
        .section-card details { margin-top: 0.25rem; }
        .section-card summary { cursor: pointer; }
        .full-document-toggle { display: flex; justify-content: flex-start; margin: 0.5rem 0; }
        .full-document-toggle label { font-weight: 400; }
        .task-controls { display: flex; flex-direction: column; align-items: flex-start; gap: 0.5rem; margin: 0.5rem 0; }
        .task-select { display: flex; flex-direction: column; text-align: left; }
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
            <label for="gemeinde">Gemeinde</label><br>
            <select id="gemeinde" name="municipality">
                <option value=""></option>
                <option value="Balsthal">Balsthal</option>
                <option value="Bellach">Bellach</option>
                <option value="Biberist">Biberist</option>
                <option value="Deitingen">Deitingen</option>
                <option value="Dornach">Dornach</option>
                <option value="Dulliken">Dulliken</option>
                <option value="Erlinsbach">Erlinsbach</option>
                <option value="Gerlafingen">Gerlafingen</option>
                <option value="Gretzenbach">Gretzenbach</option>
                <option value="Härkingen">Härkingen</option>
                <option value="Himmelried">Himmelried</option>
                <option value="Kappel">Kappel</option>
                <option value="Langendorf">Langendorf</option>
                <option value="Lüsslingen-Nennigkofen">Lüsslingen-Nennigkofen</option>
                <option value="Niedergösgen">Niedergösgen</option>
                <option value="Oberbuchsiten">Oberbuchsiten</option>
                <option value="Oensingen">Oensingen</option>
                <option value="Olten">Olten</option>
                <option value="Recherswil">Recherswil</option>
                <option value="Riedholz">Riedholz</option>
                <option value="Rohr">Rohr</option>
                <option value="Schönenwerd">Schönenwerd</option>
                <option value="Selzach">Selzach</option>
                <option value="Solothurn">Solothurn</option>
                <option value="Starrkirch-Wil">Starrkirch-Wil</option>
                <option value="Stüsslingen">Stüsslingen</option>
                <option value="Trimbach">Trimbach</option>
                <option value="Zuchwil">Zuchwil</option>
            </select><br>
            <label for="planungsinstrument">Planungsinstrument</label><br>
            <select id="planungsinstrument" name="plan_type">
                <option value=""></option>
                <option value="gestaltungsplan">gestaltungsplan</option>
                <option value="ortsplanung">ortsplanung</option>
            </select><br>
            <label for="lexicalWeight">Lexikalisches Gewicht (0 = semantisch, 1 = lexikalisch):</label><br>
            <input type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.6">
            <span id="lexicalWeightValue">0.60</span><br>
            <button type="submit">Suchen</button>
        </form>
        <div id="search-results">
            <p>Keine Suche ausgeführt.</p>
        </div>
    </div>
    <div class="column">
        <h2>Schritt 2: Kontext wählen und Aufgabe stellen</h2>
        <form hx-post="/task" hx-target="#task-panel" hx-swap="innerHTML">
            <label for="prompt">Aufgabe (z.B. auch Combobox für Templates für Strukturierung, damit man das nicht immer wiederholen muss)</label><br>
            <textarea id="prompt" name="prompt" rows="4" cols="50" required>Fasse die ausgewählten Abschnitte zum Thema [THEMA] in [Gestaltungsplänen] zusammen.</textarea><br>
            <div class="task-controls">
                <div class="task-select">
                    <label for="model">Modell</label>
                    <select id="model" name="model">
                        <option value="gpt-4o-mini">gpt-4o-mini</option>
                        <option value="gpt-5-mini">gpt-5-mini</option>
                    </select>
                </div>
                <div class="task-select">
                    <label for="template">Templates</label>
                    <select id="template" name="template">
                    </select>
                </div>
            </div>
            <div class="full-document-toggle">
                <label>
                    <input type="checkbox" id="useFullDocuments" name="useFullDocuments">
                    Gesamte Dokument(e) verwenden
                </label>
            </div>
            <button type="submit">Antwort generieren</button>
        </form>
        <div id="task-panel">
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
        const setChunkSelection = (scope, checked) => {
            scope.querySelectorAll('.chunk-selection').forEach((checkbox) => {
                checkbox.checked = checked;
            });
        };

        const setSectionSelection = (sectionCard, checked) => {
            setChunkSelection(sectionCard, checked);
            updateSectionState(sectionCard);
        };

        const setDocumentSelection = (documentCard, checked) => {
            documentCard.querySelectorAll('[data-section-card]').forEach((sectionCard) => {
                setSectionSelection(sectionCard, checked);
            });
            const documentToggle = documentCard.querySelector('[data-document-toggle]');
            if (documentToggle) {
                documentToggle.checked = checked;
                documentToggle.indeterminate = false;
            }
            updateDocumentState(documentCard);
        };

        const updateSectionState = (sectionCard) => {
            const sectionToggle = sectionCard.querySelector('[data-section-toggle]');
            const chunkCheckboxes = Array.from(sectionCard.querySelectorAll('.chunk-selection'));
            const checkedChunks = chunkCheckboxes.filter((checkbox) => checkbox.checked);
            if (!sectionToggle) {
                return;
            }
            sectionToggle.checked = chunkCheckboxes.length > 0 && checkedChunks.length === chunkCheckboxes.length;
            sectionToggle.indeterminate = checkedChunks.length > 0 && checkedChunks.length < chunkCheckboxes.length;
        };

        const updateDocumentState = (documentCard) => {
            const documentToggle = documentCard.querySelector('[data-document-toggle]');
            const sectionToggles = Array.from(documentCard.querySelectorAll('[data-section-toggle]'));
            if (!documentToggle || sectionToggles.length === 0) {
                return;
            }
            const allSectionsSelected = sectionToggles.every((toggle) => toggle.checked);
            const anySectionSelected = sectionToggles.some((toggle) => toggle.checked || toggle.indeterminate);
            documentToggle.checked = allSectionsSelected;
            documentToggle.indeterminate = anySectionSelected && !allSectionsSelected;
        };

        const refreshDocumentStates = () => {
            searchResults.querySelectorAll('[data-document-card]').forEach((card) => {
                card.querySelectorAll('[data-section-card]').forEach(updateSectionState);
                updateDocumentState(card);
            });
        };

        searchResults.addEventListener('click', (event) => {
            const target = event.target;
            if (target && target.matches('[data-select-all]')) {
                const sectionCards = Array.from(searchResults.querySelectorAll('[data-section-card]'));
                const sectionToggles = Array.from(searchResults.querySelectorAll('[data-section-toggle]'));
                const allSectionsSelected = sectionToggles.length > 0 && sectionToggles.every((toggle) => toggle.checked);
                const newState = !allSectionsSelected;

                sectionCards.forEach((card) => setSectionSelection(card, newState));

                const selectLabel = target.dataset.labelSelect || 'Alle Abschnitte auswählen';
                const deselectLabel = target.dataset.labelDeselect || 'Alle Abschnitte abwählen';
                target.textContent = newState ? deselectLabel : selectLabel;
                target.setAttribute('aria-pressed', String(newState));
            }
        });

        searchResults.addEventListener('change', (event) => {
            const target = event.target;
            if (target && target.matches('[data-document-toggle]')) {
                const documentCard = target.closest('[data-document-card]');
                if (!documentCard) {
                    return;
                }
                setDocumentSelection(documentCard, target.checked);
            }

            if (target && target.matches('[data-section-toggle]')) {
                const sectionCard = target.closest('[data-section-card]');
                if (!sectionCard) {
                    return;
                }
                setSectionSelection(sectionCard, target.checked);
                const documentCard = sectionCard.closest('[data-document-card]');
                if (documentCard) {
                    updateDocumentState(documentCard);
                }
            }

            if (target && target.matches('.chunk-selection')) {
                const sectionCard = target.closest('[data-section-card]');
                if (sectionCard) {
                    updateSectionState(sectionCard);
                    const documentCard = sectionCard.closest('[data-document-card]');
                    if (documentCard) {
                        updateDocumentState(documentCard);
                    }
                }
            }
        });

        const taskForm = document.querySelector('form[hx-post="/task"]');
        if (taskForm) {
            taskForm.addEventListener('htmx:configRequest', (event) => {
                const sectionToggles = Array.from(searchResults.querySelectorAll('[data-section-toggle]:checked'));

                const sectionIds = sectionToggles
                        .map((toggle) => toggle.value)
                        .filter((value) => value && value.trim() !== '');

                if (sectionIds.length === 0) {
                    event.preventDefault();
                    return;
                }

                const fullDocumentToggle = document.getElementById('useFullDocuments');

                const useFullDocuments = fullDocumentToggle ? fullDocumentToggle.checked : false;

                event.detail.parameters = {
                    ...event.detail.parameters,
                    sectionIds: sectionIds,
                    useFullDocuments: useFullDocuments,
                };
            });
        }
    }
</script>
</body>
</html>
