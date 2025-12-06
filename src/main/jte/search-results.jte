@param java.util.List<ch.so.arp.rag.chat.DocumentResult> results
<div class="flex items-center justify-between gap-4">
    <div>
        <h3 class="text-lg font-semibold text-slate-900">Gefundene Abschnitte</h3>
        <p class="text-sm text-slate-600">Wähle passende Abschnitte oder ganze Dokumente aus.</p>
    </div>
    @if(!results.isEmpty())
        <button type="button" data-select-all data-label-select="Alle Abschnitte auswählen"
                data-label-deselect="Alle Abschnitte abwählen" aria-pressed="false"
                class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-sky-200">Alle Abschnitte auswählen</button>
    @endif
</div>

@if(results.isEmpty())
    <p class="mt-3 text-sm text-slate-600">Keine Treffer gefunden.</p>
@else
    <div class="mt-4 space-y-4">
        @for(ch.so.arp.rag.chat.DocumentResult document : results)
            <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm" data-document-card data-document-id="${document.documentId().toString()}">
                <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                    <label class="flex items-start gap-3 text-sm font-semibold text-slate-800">
                        <input type="checkbox" data-document-toggle aria-label="Dokument auswählen"
                               id="document-${document.documentId().toString()}"
                               class="mt-1 h-4 w-4 rounded border-slate-300 text-sky-700 focus:ring-2 focus:ring-sky-200">
                        <span>
                            <span class="block text-base font-semibold text-slate-900">${document.title()}</span>
                            <span class="text-xs text-slate-500">${document.filename()}</span>
                        </span>
                    </label>
                    <div class="text-xs font-semibold text-emerald-700">
                        @if(document.bestHybridScore() != null)
                            <span class="rounded-full bg-emerald-50 px-3 py-1">Höchste Bewertung: ${String.format("%.2f", document.bestHybridScore())}</span>
                        @endif
                    </div>
                </div>

                <div class="mt-3 space-y-3">
                    @for(ch.so.arp.rag.chat.SectionResult section : document.sections())
                        <div class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2" data-section-card data-section-id="${section.sectionId()}">
                            <div class="flex items-start justify-between gap-2">
                                <label class="flex items-start gap-2 text-sm font-medium text-slate-800">
                                    <input type="checkbox" data-section-toggle aria-label="Abschnitt auswählen"
                                           id="section-${section.sectionId()}" data-section-id="${section.sectionId()}"
                                           value="${section.sectionId()}"
                                           class="mt-1 h-4 w-4 rounded border-slate-300 text-sky-700 focus:ring-2 focus:ring-sky-200">
                                    <span>${section.sectionPath() != null ? section.sectionPath() : "Abschnitt"}</span>
                                </label>
                                <span class="text-xs font-semibold text-slate-500">${section.chunkIds().size()} Chunk${section.chunkIds().size() != 1 ? "s" : ""}</span>
                            </div>
                            <details class="mt-2 rounded-md bg-white px-3 py-2 text-sm text-slate-700 shadow-sm">
                                <summary class="cursor-pointer font-semibold text-slate-800">Abschnittstext anzeigen</summary>
                                <p class="mt-2 leading-relaxed">${section.sectionText()}</p>
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
        @endfor
    </div>
@endif
