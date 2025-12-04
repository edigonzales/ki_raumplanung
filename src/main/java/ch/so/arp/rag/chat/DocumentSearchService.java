package ch.so.arp.rag.chat;

import java.util.List;

public interface DocumentSearchService {

    List<DocumentResult> search(String keywords, double lexicalWeight, String municipality, String planType);

    List<DocumentChunk> findByIds(List<Long> ids);

    String hybridSearchSql();
}
