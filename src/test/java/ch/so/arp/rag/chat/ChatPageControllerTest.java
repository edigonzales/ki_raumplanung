package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class ChatPageControllerTest {

    private final ChatPageController controller = new ChatPageController(new MockHybridSearchService());

    @Test
    void shouldReturnIndexView() {
        ExtendedModelMap model = new ExtendedModelMap();
        assertThat(controller.index(model)).isEqualTo("index");
        assertThat(model.get("results")).isNotNull();
    }
}
