@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RAG Assistent</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@1.0.2/css/bulma.min.css">
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4/sse.min.js"></script>
    <style>
        body { background-color: #f9fafb; }
        .surface {
            background-color: white;
            border: 1px solid #e5e7eb;
            border-radius: 10px;
            padding: 1.25rem;
        }
        .surface + .surface { margin-top: 1rem; }
        .section-card { border-top: 1px solid #e5e7eb; padding-top: 0.75rem; margin-top: 0.75rem; }
        .section-card:first-child { border-top: none; padding-top: 0; margin-top: 0; }
        .section-card details { margin-top: 0.5rem; }
        .section-card summary { cursor: pointer; }
        .sticky-actions { position: sticky; top: 1rem; z-index: 2; }
        .subtle-label { color: #6b7280; font-size: 0.9rem; }
        .chunk-selection-container { display: none; }
        .header-divider { border-bottom: 1px solid #e5e7eb; padding-bottom: 0.75rem; margin-bottom: 1rem; }
    </style>
</head>
<body>
<section class="section">
    <div class="container">
        <div class="level">
            <div class="level-left">
                <div class="level-item">
                    <h1 class="title">RAG Assistent</h1>
                </div>
            </div>
            <div class="level-right">
                <p class="level-item is-size-7 has-text-grey">Suche · Kontext · Antwort</p>
            </div>
        </div>

        <div class="columns is-variable is-6">
            <div class="column is-half">
                <div class="surface">
                    <div class="header-divider">
                        <p class="title is-5 mb-1">Suche</p>
                        <p class="subtitle is-6 subtle-label">Stichworte eingeben, optional Gemeinde &amp; Planungsinstrument wählen.</p>
                    </div>
                    <form hx-post="/search" hx-target="#search-results" hx-swap="innerHTML">
                        <div class="field">
                            <label class="label" for="keywords">Stichworte</label>
                            <div class="control">
                                <input class="input" type="text" id="keywords" name="keywords" required placeholder="z.B. Quartierplan, Schulhaus">
                            </div>
                        </div>

                        <div class="field is-horizontal">
                            <div class="field-body">
                                <div class="field">
                                    <label class="label" for="gemeinde">Gemeinde</label>
                                    <div class="control">
                                        <div class="select is-fullwidth">
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
                                        </div>
                                    </div>
                                </div>
                                <div class="field">
                                    <label class="label" for="planungsinstrument">Planungsinstrument</label>
                                    <div class="control">
                                        <div class="select is-fullwidth">
                                            <select id="planungsinstrument" name="plan_type">
                                                <option value=""></option>
                                                <option value="gestaltungsplan">gestaltungsplan</option>
                                                <option value="ortsplanung">ortsplanung</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="field">
                            <label class="label" for="lexicalWeight">Lexikalisches Gewicht</label>
                            <p class="help">0 = semantisch, 1 = lexikalisch</p>
                            <div class="control is-flex is-align-items-center">
                                <input class="slider is-fullwidth" type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.6">
                                <span id="lexicalWeightValue" class="tag is-light ml-3">0.60</span>
                            </div>
                        </div>

                        <div class="field is-grouped is-justify-content-space-between is-align-items-center">
                            <div class="control">
                                <button class="button is-primary" type="submit">Suchen</button>
                            </div>
                            <p class="help mb-0">Liefert Abschnitte für den nächsten Schritt.</p>
                        </div>
                    </form>
                </div>

                <div class="surface" id="search-results">
                    <p class="has-text-grey">Keine Suche ausgeführt.</p>
                </div>
            </div>

            <div class="column is-half">
                <div class="surface sticky-actions">
                    <div class="header-divider">
                        <p class="title is-5 mb-1">Aufgabe &amp; Kontext</p>
                        <p class="subtitle is-6 subtle-label">Abschnitte markieren und Aufgabe formulieren.</p>
                    </div>
                    <form hx-post="/task" hx-target="#task-panel" hx-swap="innerHTML">
                        <div class="field">
                            <label class="label" for="prompt">Aufgabe</label>
                            <div class="control">
                                <textarea class="textarea" id="prompt" name="prompt" rows="4" required>Fasse die ausgewählten Abschnitte zum Thema [THEMA] in [Gestaltungsplänen] zusammen.</textarea>
                            </div>
                            <p class="help">Kurze Anweisung, z. B. Zusammenfassung oder Prüfung.</p>
                        </div>

                        <div class="field">
                            <label class="label" for="template">Templates</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select id="template" name="template" disabled>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="field">
                            <label class="label" for="model">Modell</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select id="model" name="model" disabled>
                                        <option value="gpt-4o-mini">gpt-4o-mini</option>
                                        <option value="gpt-5-mini">gpt-5-mini</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="field">
                            <label class="checkbox">
                                <input type="checkbox" id="useFullDocuments" name="useFullDocuments">
                                Gesamte Dokument(e) verwenden
                            </label>
                        </div>

                        <div class="field is-grouped is-justify-content-flex-end">
                            <p class="help mr-auto">Mindestens einen Abschnitt auswählen.</p>
                            <div class="control">
                                <button class="button is-link" type="submit">Antwort generieren</button>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="surface mt-4" id="task-panel">
                    <p class="has-text-grey">Wähle zunächst Abschnitte aus der Suche.</p>
                </div>
            </div>
        </div>
    </div>
</section>
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
