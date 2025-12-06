@param String streamUrl
@param java.util.List<ch.so.arp.rag.chat.SectionSelection> selection
@param String prompt
@if(streamUrl == null || streamUrl.isBlank())
    <div class="callout">
        <p>Bitte wähle mindestens einen Abschnitt aus.</p>
    </div>
@else
    <div class="task-summary" hx-ext="sse" sse-connect="${streamUrl}" sse-close="close">
        <div class="card-header">
            <div>
                <p class="eyebrow">Aufgabe</p>
                <h3>${prompt}</h3>
            </div>
            <div class="pill">${selection.size()} ${selection.stream().allMatch(selected -> selected.sectionId() == null) ? "Dokumente" : "Abschnitte"}</div>
        </div>
        <ul class="selection-list">
            @for(ch.so.arp.rag.chat.SectionSelection selected : selection)
                <li>
                    <span class="muted">${selected.title()}</span>
                    <strong>${selected.sectionPath() != null ? selected.sectionPath() : "Ganzes Dokument"}</strong>
                </li>
            @endfor
        </ul>
        <div id="task-output" class="task-output" sse-swap="message">
            <p>Antwort wird geladen...</p>
        </div>
    </div>
@endif
