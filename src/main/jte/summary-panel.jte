@param String streamUrl
@param java.util.List<ch.so.arp.rag.chat.DocumentChunk> selection
@param String prompt
@if(streamUrl == null || streamUrl.isBlank()) {
    <p>Bitte wähle mindestens einen Abschnitt aus.</p>
@else {
    <div hx-ext="sse" sse-connect="${streamUrl}">
        <p>Aufgabe: ${prompt}</p>
        <p>Kontext: ${selection.size()} Abschnitte</p>
        <ul>
            @for(ch.so.arp.rag.chat.DocumentChunk chunk : selection) {
                <li>${chunk.title()} – ${chunk.sectionPath()}</li>
            @endfor
        </ul>
        <div id="summary-output" sse-swap="message">
            <p>Antwort wird geladen...</p>
        </div>
    </div>
@endif
