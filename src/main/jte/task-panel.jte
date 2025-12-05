@param String streamUrl
@param java.util.List<ch.so.arp.rag.chat.SectionSelection> selection
@param String prompt
@if(streamUrl == null || streamUrl.isBlank())
    <p>Bitte wähle mindestens einen Abschnitt aus.</p>
@else
    <div hx-ext="sse" sse-connect="${streamUrl}" sse-close="close">
        <p>Aufgabe: ${prompt}</p>
        <p>Kontext: ${selection.size()} ${selection.stream().allMatch(selected -> selected.sectionId() == null) ? "Dokumente" : "Abschnitte"}</p>
        <ul>
            @for(ch.so.arp.rag.chat.SectionSelection selected : selection)
                <li>
                    ${selected.title()} –
                    ${selected.sectionPath() != null ? selected.sectionPath() : "Ganzes Dokument"}
                </li>
            @endfor
        </ul>
        <div id="task-output" sse-swap="message">
            <p>Antwort wird geladen...</p>
        </div>
    </div>
@endif
