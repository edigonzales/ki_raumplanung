@param String streamUrl
@param java.util.List<ch.so.arp.rag.chat.SectionSelection> selection
@param String prompt
@if(streamUrl == null || streamUrl.isBlank())
    <div class="notification is-light">Bitte wähle mindestens einen Abschnitt aus.</div>
@else
    <div hx-ext="sse" sse-connect="${streamUrl}" sse-close="close">
        <div class="content">
            <p><strong>Aufgabe:</strong> ${prompt}</p>
            <p><strong>Kontext:</strong> ${selection.size()} ${selection.stream().allMatch(selected -> selected.sectionId() == null) ? "Dokumente" : "Abschnitte"}</p>
            <ul>
                @for(ch.so.arp.rag.chat.SectionSelection selected : selection)
                    <li>
                        ${selected.title()} –
                        ${selected.sectionPath() != null ? selected.sectionPath() : "Ganzes Dokument"}
                    </li>
                @endfor
            </ul>
        </div>
        <div id="task-output" class="box" sse-swap="message">
            <p>Antwort wird geladen...</p>
        </div>
    </div>
@endif
