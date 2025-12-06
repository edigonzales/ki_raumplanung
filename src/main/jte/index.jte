@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>RAG Assistent</title>
    <link rel="stylesheet" href="/fonts.css">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    <script src="https://cdn.jsdelivr.net/npm/htmx-ext-sse@2.2.4/sse.min.js"></script>
    <style>
        body { font-family: Frutiger, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
    </style>
</head>
<body>
<div class="min-h-screen bg-slate-50 text-slate-900">
    <header class="bg-gradient-to-r from-slate-900 via-sky-900 to-sky-700 text-white shadow-lg">
        <div class="mx-auto max-w-6xl px-6 py-8 flex items-center gap-4">
            <img src="/Logo.png" alt="Logo" class="h-12 w-12 rounded-full bg-white/10 p-2">
            <div>
                <p class="text-sm uppercase tracking-[0.2em] text-slate-200">Räumliche Planung & KI</p>
                <h1 class="text-3xl font-semibold leading-tight">RAG Assistent</h1>
                <p class="text-slate-200">Finde relevante Dokumente, wähle Abschnitte und lass dir Antworten generieren.</p>
            </div>
        </div>
    </header>

    <main class="mx-auto max-w-6xl px-6 py-10 space-y-8">
        <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <section class="rounded-2xl border border-slate-200 bg-white/80 p-6 shadow-sm backdrop-blur">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <p class="text-sm font-medium text-sky-700">Schritt 1</p>
                        <h2 class="text-xl font-semibold text-slate-900">Suche konfigurieren</h2>
                        <p class="text-sm text-slate-600">Filter anwenden und Treffer abrufen.</p>
                    </div>
                    <span class="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-700">Suche</span>
                </div>

                <form class="mt-6 space-y-4" hx-post="/search" hx-target="#search-results" hx-swap="innerHTML">
                    <div>
                        <label for="keywords" class="block text-sm font-medium text-slate-700">Stichworte</label>
                        <input type="text" id="keywords" name="keywords" required
                               class="mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-200">
                    </div>
                    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div>
                            <label for="gemeinde" class="block text-sm font-medium text-slate-700">Gemeinde</label>
                            <select id="gemeinde" name="municipality"
                                    class="mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-200">
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
                        <div>
                            <label for="planungsinstrument" class="block text-sm font-medium text-slate-700">Planungsinstrument</label>
                            <select id="planungsinstrument" name="plan_type"
                                    class="mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-200">
                                <option value=""></option>
                                <option value="gestaltungsplan">gestaltungsplan</option>
                                <option value="ortsplanung">ortsplanung</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <div class="flex items-center justify-between text-sm font-medium text-slate-700">
                            <label for="lexicalWeight">Lexikalisches Gewicht</label>
                            <span class="text-slate-500">0 = semantisch, 1 = lexikalisch</span>
                        </div>
                        <div class="mt-2 flex items-center gap-3">
                            <input type="range" id="lexicalWeight" name="lexicalWeight" min="0" max="1" step="0.05" value="0.6"
                                   class="w-full accent-sky-600">
                            <span id="lexicalWeightValue" class="w-12 rounded-md bg-slate-100 px-2 py-1 text-center text-sm font-semibold text-slate-800">0.60</span>
                        </div>
                    </div>
                    <div class="flex justify-end gap-3">
                        <button type="reset" class="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-sky-200">Zurücksetzen</button>
                        <button type="submit" class="rounded-lg bg-sky-700 px-4 py-2 text-sm font-semibold text-white shadow-md transition hover:bg-sky-800 focus:outline-none focus:ring-2 focus:ring-sky-200">Suchen</button>
                    </div>
                </form>

                <div id="search-results" class="mt-6 rounded-xl border border-dashed border-slate-200 bg-slate-50/70 p-4 text-sm text-slate-600">
                    <p>Keine Suche ausgeführt.</p>
                </div>
            </section>

            <section class="rounded-2xl border border-slate-200 bg-white/80 p-6 shadow-sm backdrop-blur">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <p class="text-sm font-medium text-sky-700">Schritt 2</p>
                        <h2 class="text-xl font-semibold text-slate-900">Kontext wählen & Aufgabe stellen</h2>
                        <p class="text-sm text-slate-600">Abschnitte auswählen und Antwort generieren.</p>
                    </div>
                    <span class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">Aufgabe</span>
                </div>

                <form class="mt-6 space-y-4" hx-post="/task" hx-target="#task-panel" hx-swap="innerHTML">
                    <div>
                        <label for="prompt" class="block text-sm font-medium text-slate-700">Aufgabe</label>
                        <textarea id="prompt" name="prompt" rows="4" required
                                  class="mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-900 shadow-sm focus:border-sky-500 focus:ring-2 focus:ring-sky-200">Fasse die ausgewählten Abschnitte zum Thema [THEMA] in [Gestaltungsplänen] zusammen.</textarea>
                        <p class="mt-1 text-xs text-slate-500">Tipp: Lege Templates an, um häufige Aufgaben schneller zu starten.</p>
                    </div>

                    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div class="space-y-1">
                            <label for="template" class="block text-sm font-medium text-slate-700">Templates</label>
                            <select id="template" name="template" disabled
                                    class="w-full rounded-lg border border-slate-200 bg-slate-100 px-3 py-2 text-slate-500 shadow-sm">
                            </select>
                        </div>
                        <div class="space-y-1">
                            <label for="model" class="block text-sm font-medium text-slate-700">Modell</label>
                            <select id="model" name="model" disabled
                                    class="w-full rounded-lg border border-slate-200 bg-slate-100 px-3 py-2 text-slate-500 shadow-sm">
                                <option value="gpt-4o-mini">gpt-4o-mini</option>
                                <option value="gpt-5-mini">gpt-5-mini</option>
                            </select>
                        </div>
                    </div>

                    <label class="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-medium text-slate-800 shadow-sm">
                        <input type="checkbox" id="useFullDocuments" name="useFullDocuments" class="h-4 w-4 rounded border-slate-300 text-emerald-600 focus:ring-2 focus:ring-emerald-200">
                        Gesamte Dokumente verwenden
                    </label>

                    <div class="flex justify-end gap-3">
                        <button type="submit" class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white shadow-md transition hover:bg-emerald-700 focus:outline-none focus:ring-2 focus:ring-emerald-200">Antwort generieren</button>
                    </div>
                </form>

                <div id="task-panel" class="mt-6 rounded-xl border border-dashed border-slate-200 bg-slate-50/70 p-4 text-sm text-slate-700">
                    <p>Wähle zunächst Abschnitte aus der Suche.</p>
                </div>
            </section>
        </div>
    </main>
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
