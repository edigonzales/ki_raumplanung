package ch.so.arp.rag.chat;

import java.util.List;
import java.util.UUID;

public interface DocumentSearchService {

    List<DocumentResult> search(String keywords, double lexicalWeight, String municipality, String planType);

    List<SectionSelection> findBySectionSelections(List<Long> sectionIds, boolean useFullDocuments);

    String hybridSearchSql();
}
