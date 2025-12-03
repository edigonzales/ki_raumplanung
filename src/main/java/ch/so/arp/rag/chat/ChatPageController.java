package ch.so.arp.rag.chat;

import java.util.Collections;

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

    public ChatPageController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("results", Collections.emptyList());
        return "index";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute @Validated SearchRequest request, Model model) {
        model.addAttribute("results", searchService.search(request.keywords(), request.lexicalWeight()));
        return "search-results";
    }

    @PostMapping("/summary")
    public String summary(@ModelAttribute @Validated SummaryForm form, Model model, UriComponentsBuilder uriBuilder) {
        var selection = searchService.findByIds(form.selection());
        if (selection.isEmpty()) {
            model.addAttribute("streamUrl", "");
            model.addAttribute("selection", selection);
            model.addAttribute("prompt", form.prompt());
            return "summary-panel";
        }
        String streamUrl = uriBuilder.path("/api/chat/summary-stream")
                .queryParam("prompt", form.prompt())
                .queryParam("selection", form.selection())
                .build()
                .toUriString();

        model.addAttribute("streamUrl", streamUrl);
        model.addAttribute("selection", selection);
        model.addAttribute("prompt", form.prompt());
        return "summary-panel";
    }
}
