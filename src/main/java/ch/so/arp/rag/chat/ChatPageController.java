package ch.so.arp.rag.chat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Serves the simple debug chat page backed by the JTE template.
 */
@Controller
@Validated
public class ChatPageController {

    private final DocumentSearchService searchService;
    private final TaskContextStore contextStore;

    public ChatPageController(DocumentSearchService searchService, TaskContextStore contextStore) {
        this.searchService = searchService;
        this.contextStore = contextStore;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("results", Collections.emptyList());
        return "index";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute @Validated SearchRequest request, Model model) {
        model.addAttribute(
                "results",
                searchService.search(
                        request.keywords(), request.lexicalWeight(), request.municipality(), request.planType()));
        return "search-results";
    }

    @PostMapping("/task")
    public String task(@ModelAttribute @Validated TaskForm form, Model model, UriComponentsBuilder uriBuilder) {
        List<Long> sectionIds = form.sectionIds() == null ? Collections.emptyList() : form.sectionIds();

        var selection = searchService.findBySectionSelections(sectionIds, form.useFullDocuments());
        if (selection.isEmpty()) {
            model.addAttribute("streamUrl", "");
            model.addAttribute("selection", selection);
            model.addAttribute("prompt", form.prompt());
            return "task-panel";
        }

        List<Long> effectiveSectionIds = selection.stream()
                .map(SectionSelection::sectionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<java.util.UUID> effectiveDocumentIds = selection.stream()
                .filter(selected -> selected.sectionId() == null)
                .map(SectionSelection::documentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        java.util.UUID contextToken = contextStore.store(selection);

        String streamUrl = uriBuilder.path("/api/chat/task-stream")
                .queryParam("prompt", form.prompt())
                .queryParam("sectionIds", effectiveSectionIds)
                .queryParam("documentIds", effectiveDocumentIds)
                .queryParam("contextToken", contextToken)
                .build()
                .toUriString();

        model.addAttribute("streamUrl", streamUrl);
        model.addAttribute("selection", selection);
        model.addAttribute("prompt", form.prompt());
        return "task-panel";
    }
}
