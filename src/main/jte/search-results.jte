@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<div class="card">
    <div class="card-header results-header">
        <div>
            <p class="eyebrow">Gefundene Abschnitte</p>
            <h2>Trefferübersicht</h2>
        </div>
        @if(!results.isEmpty())
            <button type="button" class="btn ghost" data-select-all data-label-select="Alle Abschnitte auswählen"
                    data-label-deselect="Alle Abschnitte abwählen" aria-pressed="false">Alle Abschnitte auswählen</button>
        @endif
    </div>
    @if(results.isEmpty())
        <p class="helper">Keine Treffer gefunden. Bitte passe die Suche oder Filter an.</p>
    @else
        <div class="result-list">
            @for(ch.so.arp.rag.chat.DocumentResult document : results)
                <article class="result-card" data-document-card data-document-id="${document.documentId().toString()}">
                    <header class="document-header">
                        <div class="checkbox-row">
                            <input type="checkbox" data-document-toggle aria-label="Dokument auswählen"
                                   id="document-${document.documentId().toString()}">
                            <div>
                                <strong>${document.title()}</strong>
                                <p class="muted">${document.filename()}</p>
                            </div>
                        </div>
                        <div class="document-meta">
                            @if(document.bestHybridScore() != null)
                                <span class="pill">Score ${String.format("%.2f", document.bestHybridScore())}</span>
                            @endif
                        </div>
                    </header>
                    <div class="section-list">
                        @for(ch.so.arp.rag.chat.SectionResult section : document.sections())
                            <div class="section-card" data-section-card data-section-id="${section.sectionId()}">
                                <div class="section-header">
                                    <label class="checkbox-row">
                                        <input type="checkbox" data-section-toggle aria-label="Abschnitt auswählen"
                                               id="section-${section.sectionId()}" data-section-id="${section.sectionId()}"
                                               value="${section.sectionId()}">
                                        <div>
                                            <strong>${section.sectionPath() != null ? section.sectionPath() : "Abschnitt"}</strong>
                                            <p class="muted">Abschnitt auswählen oder aufklappen</p>
                                        </div>
                                    </label>
                                </div>
                                <details class="section-body">
                                    <summary>Abschnittstext anzeigen</summary>
                                    <p>${section.sectionText()}</p>
                                </details>
                                <div class="chunk-selection-container">
                                    @for(Long chunkId : section.chunkIds())
                                        <input type="checkbox" class="chunk-selection" value="${chunkId}" hidden>
                                    @endfor
                                </div>
                            </div>
                        @endfor
                    </div>
                </article>
            @endfor
        </div>
    @endif
</div>
