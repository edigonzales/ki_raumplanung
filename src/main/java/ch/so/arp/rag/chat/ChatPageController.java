package ch.so.arp.rag.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the simple debug chat page backed by the JTE template.
 */
@Controller
public class ChatPageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
