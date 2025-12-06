@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RAG Assistent</title>
    <link rel="stylesheet" href="/fonts.css">
    <link rel="stylesheet" href="/style.css">
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4/sse.min.js"></script>
</head>
<body>
<header class="app-header">
    <div class="brand">
        <img src="/Logo.png" alt="Logo" class="brand-mark" loading="lazy">
        <div class="brand-text">
            <span class="eyebrow">Raumplanung AI</span>
            <span class="title">RAG Assistent</span>
        </div>
    </div>
    <nav class="top-nav" aria-label="Sekundäre Navigation">
        <a href="#hilfe">Hilfe</a>
        <a href="#kontakt">Kontakt</a>
    </nav>
</header>
<main class="page">
    <div class="layout">
        <section class="column">
            <div class="card">
                <div class="card-header">
                    <div>
                        <p class="eyebrow">Schritt 1</p>
                        <h1>Suche</h1>
                        <p class="helper">Freitextsuche nach Stichworten und Filtern. Ergebnisse erscheinen darunter.</p>
                    </div>
                </div>
                <form class="stack" hx-post="/search" hx-target="#search-results" hx-swap="innerHTML">
                    <label class="field">
                        <span class="label">Stichworte</span>
                        <div class="input-with-icon">
                            <span class="icon">🔍</span>
                            <input type="text" id="keywords" name="keywords" required placeholder="z.B. Gestaltungsplan Areal">
                        </div>
                        <span class="helper">Freitextsuche nach Stichworten.</span>
                    </label>
                    <div class="form-grid">
                        <label class="field">
                            <span class="label">Gemeinde</span>
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
                            </select>
                            <span class="helper">Optional nach Gemeinde filtern.</span>
                        </label>
                        <label class="field">
                            <span class="label">Planungsinstrument</span>
                            <select id="planungsinstrument" name="plan_type">
                                <option value=""></option>
                                <option value="gestaltungsplan">gestaltungsplan</option>
                                <option value="ortsplanung">ortsplanung</option>
                            </select>
                            <span class="helper">Optional nach Dokumentart filtern.</span>
                        </label>
                    </div>
                    <label class="field slider-field">
                        <div class="label-row">
                            <span class="label">Lexikalisches Gewicht</span>
                            <span class="badge">0 = semantisch · 1 = lexikalisch</span>
                        </div>
                        <div class="slider-row">
                            <input type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.6">
                            <span class="value" id="lexicalWeightValue">0.60</span>
                        </div>
                    </label>
                    <div class="actions">
                        <button class="btn primary" type="submit">Suchen</button>
                        <button class="btn ghost" type="reset">Zurücksetzen</button>
                    </div>
                </form>
            </div>
            <div id="search-results" class="stack results-shell">
                <div class="card muted">
                    <div class="card-header">
                        <h2>Gefundene Abschnitte</h2>
                        <span class="pill">Keine Suche ausgeführt</span>
                    </div>
                    <p class="helper">Starte eine Suche, um Abschnitte auszuwählen.</p>
                </div>
            </div>
        </section>
        <section class="column">
            <div class="card sticky">
                <div class="card-header">
                    <div>
                        <p class="eyebrow">Schritt 2</p>
                        <h1>Kontext wählen & Aufgabe stellen</h1>
                        <p class="helper">Wähle zunächst Abschnitte oder ganze Dokumente. Danach kannst du eine Aufgabe definieren.</p>
                    </div>
                </div>
                <form class="stack" hx-post="/task" hx-target="#task-panel" hx-swap="innerHTML">
                    <label class="field">
                        <span class="label">Aufgabe</span>
                        <textarea id="prompt" name="prompt" rows="6" required
                                  placeholder="Fasse die ausgewählten Abschnitte zum Thema [THEMA] in [Gestaltungsplänen] zusammen.">Fasse die ausgewählten Abschnitte zum Thema [THEMA] in [Gestaltungsplänen] zusammen.</textarea>
                        <div class="field-footer">
                            <span class="helper">Gib deine Frage oder Anweisung ein. Templates folgen.</span>
                            <span class="char-count" id="promptCounter">0 Zeichen</span>
                        </div>
                    </label>
                    <div class="stack compact">
                        <label class="field">
                            <span class="label">Templates</span>
                            <select id="template" name="template" disabled>
                            </select>
                        </label>
                        <label class="field">
                            <span class="label">Modell</span>
                            <select id="model" name="model" disabled>
                                <option value="gpt-4o-mini">gpt-4o-mini</option>
                                <option value="gpt-5-mini">gpt-5-mini</option>
                            </select>
                        </label>
                    </div>
                    <label class="checkbox-field">
                        <input type="checkbox" id="useFullDocuments" name="useFullDocuments">
                        <span>Gesamte Dokument(e) verwenden</span>
                    </label>
                    <div class="actions">
                        <button class="btn primary" type="submit">Antwort generieren</button>
                    </div>
                </form>
                <div id="task-panel" class="stack">
                    <div class="callout">
                        <p>Wähle zunächst Abschnitte aus der Suche.</p>
                    </div>
                </div>
            </div>
        </section>
    </div>
</main>
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

    const promptInput = document.getElementById('prompt');
    const promptCounter = document.getElementById('promptCounter');
    if (promptInput && promptCounter) {
        const updateCounter = () => {
            promptCounter.textContent = `${'$'}{promptInput.value.length} Zeichen`;
        };
        promptInput.addEventListener('input', updateCounter);
        updateCounter();
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

        // Ensure card states stay in sync when HTMX refreshes results
        document.body.addEventListener('htmx:afterSwap', (evt) => {
            if (evt.target && evt.target.id === 'search-results') {
                refreshDocumentStates();
            }
        });
    }
</script>
</body>
</html>
