@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<h3>Gefundene Abschnitte</h3>
@if(results.isEmpty()) 
    <p>Keine Treffer gefunden.</p>
@else 
    <button type="button" data-select-all data-label-select="Alle Abschnitte auswählen"
            data-label-deselect="Alle Abschnitte abwählen" aria-pressed="false">Alle Abschnitte auswählen</button>
    @for(ch.so.arp.rag.chat.DocumentResult document : results)
        <div class="result-card" data-document-card>
            <div class="document-header">
                <label>
                    <input type="checkbox" data-document-toggle aria-label="Dokument auswählen"
                           id="document-${document.documentId()}">
                    <strong>${document.title()}</strong> (${document.filename()})
                </label>
                <div class="document-meta">
                    @if(document.bestHybridScore() != null) 
                        <small>Höchste Bewertung: ${String.format("%.2f", document.bestHybridScore())}</small>
                    @endif
                </div>
            </div>
            <div class="section-list">
                @for(ch.so.arp.rag.chat.SectionResult section : document.sections())
                    <div class="section-card" data-section-card>
                        <label>
                            <input type="checkbox" data-section-toggle aria-label="Abschnitt auswählen"
                                   id="section-${section.sectionId()}" data-section-id="${section.sectionId()}">
                            <strong>${section.sectionPath() != null ? section.sectionPath() : "Abschnitt"}</strong>
                        </label>
                        <details>
                            <summary>Abschnittstext anzeigen</summary>
                            <p>${section.sectionText()}</p>
                        </details>
                        <div class="chunk-selection-container">
                            @for(Long chunkId : section.chunkIds()) 
                                <input type="checkbox" class="chunk-selection" name="selection" value="${chunkId}" hidden>
                            @endfor
                        </div>
                    </div>
                @endfor
            </div>
        </div>
    @endfor
@endif
