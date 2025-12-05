@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<div class="is-flex is-align-items-center is-justify-content-space-between mb-3">
    <h3 class="title is-6 mb-0">Gefundene Abschnitte</h3>
    @if(!results.isEmpty())
        <button class="button is-small is-link is-light" type="button" data-select-all data-label-select="Alle Abschnitte auswählen"
                data-label-deselect="Alle Abschnitte abwählen" aria-pressed="false">Alle Abschnitte auswählen</button>
    @endif
</div>

@if(results.isEmpty())
    <div class="notification is-light">Keine Treffer gefunden.</div>
@else
    @for(ch.so.arp.rag.chat.DocumentResult document : results)
        <div class="card result-card" data-document-card data-document-id="${document.documentId().toString()}">
            <header class="card-header">
                <p class="card-header-title is-justify-content-space-between">
                    <span>
                        <label class="checkbox">
                            <input type="checkbox" data-document-toggle aria-label="Dokument auswählen"
                                   id="document-${document.documentId().toString()}">
                            <strong>${document.title()}</strong>
                        </label>
                        <span class="is-size-7 has-text-grey">(${document.filename()})</span>
                    </span>
                    @if(document.bestHybridScore() != null)
                        <span class="tag is-light">Score ${String.format("%.2f", document.bestHybridScore())}</span>
                    @endif
                </p>
            </header>
            <div class="card-content">
                <div class="section-list">
                    @for(ch.so.arp.rag.chat.SectionResult section : document.sections())
                        <div class="box section-card" data-section-card data-section-id="${section.sectionId()}">
                            <label class="checkbox">
                                <input type="checkbox" data-section-toggle aria-label="Abschnitt auswählen"
                                       id="section-${section.sectionId()}" data-section-id="${section.sectionId()}"
                                       value="${section.sectionId()}">
                                <strong>${section.sectionPath() != null ? section.sectionPath() : "Abschnitt"}</strong>
                            </label>
                            <details class="mt-2">
                                <summary>Abschnittstext anzeigen</summary>
                                <p class="mt-2">${section.sectionText()}</p>
                            </details>
                            <div class="chunk-selection-container">
                                @for(Long chunkId : section.chunkIds())
                                    <input type="checkbox" class="chunk-selection" value="${chunkId}" hidden>
                                @endfor
                            </div>
                        </div>
                    @endfor
                </div>
            </div>
        </div>
    @endfor
@endif
