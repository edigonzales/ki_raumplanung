package ch.so.arp.rag.chat;

import java.util.List;

public interface DocumentSearchService {

    List<DocumentChunk> search(String keywords);

    List<DocumentChunk> findByIds(List<Long> ids);

    String hybridSearchSql();
}
