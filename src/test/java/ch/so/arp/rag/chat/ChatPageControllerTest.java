package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatPageControllerTest {

    private final ChatPageController controller = new ChatPageController();

    @Test
    void shouldReturnIndexView() {
        assertThat(controller.index()).isEqualTo("index");
    }
}
