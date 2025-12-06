@param String streamUrl
@param java.util.List<ch.so.arp.rag.chat.SectionSelection> selection
@param String prompt
@if(streamUrl == null || streamUrl.isBlank())
    <div class="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800 shadow-sm">
        Bitte wähle mindestens einen Abschnitt aus.
    </div>
@else
    <div class="space-y-4" hx-ext="sse" sse-connect="${streamUrl}" sse-close="close">
        <div class="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900 shadow-sm">
            <p class="font-semibold">Aufgabe</p>
            <p class="text-slate-800">${prompt}</p>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm">
            <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-slate-800">Kontext</p>
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">${selection.size()} ${selection.stream().allMatch(selected -> selected.sectionId() == null) ? "Dokumente" : "Abschnitte"}</span>
            </div>
            <ul class="mt-2 space-y-1 text-sm text-slate-700">
                @for(ch.so.arp.rag.chat.SectionSelection selected : selection)
                    <li class="flex items-start gap-2">
                        <span class="mt-0.5 h-2 w-2 rounded-full bg-sky-600"></span>
                        <span>
                            <span class="block font-semibold text-slate-900">${selected.title()}</span>
                            <span class="text-slate-600">${selected.sectionPath() != null ? selected.sectionPath() : "Ganzes Dokument"}</span>
                        </span>
                    </li>
                @endfor
            </ul>
        </div>

        <div id="task-output" sse-swap="message" class="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-800 shadow-inner">
            <p>Antwort wird geladen...</p>
        </div>
    </div>
@endif
