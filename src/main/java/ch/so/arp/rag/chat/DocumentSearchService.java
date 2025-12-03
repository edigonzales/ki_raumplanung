package ch.so.arp.rag.chat;

import java.util.List;

public interface DocumentSearchService {

    List<DocumentChunk> search(String keywords, double lexicalWeight);

    List<DocumentChunk> findByIds(List<Long> ids);

    String hybridSearchSql();
}
