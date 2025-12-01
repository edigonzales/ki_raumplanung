@param java.util.List<ch.so.arp.rag.chat.DocumentChunk> results
<h3>Gefundene Abschnitte</h3>
@if(results.isEmpty()) {
    <p>Keine Treffer gefunden.</p>
@endif
@for(ch.so.arp.rag.chat.DocumentChunk chunk : results) {
    <div class="result-card">
        <label>
            <input type="checkbox" name="selection" value="${chunk.id()}">
            <strong>${chunk.title()}</strong> (${chunk.filename()})<br>
            <em>${chunk.sectionPath()}</em>
        </label>
        <p>${chunk.snippet()}</p>
    </div>
@endfor
