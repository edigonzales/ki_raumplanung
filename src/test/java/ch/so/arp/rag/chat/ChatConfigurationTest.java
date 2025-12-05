package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;

class ChatConfigurationTest {

    private final ChatConfiguration configuration = new ChatConfiguration();

    @Test
    void usesBuilderWhenChatClientBeanMissing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.ai.openai.api-key", "test-key");

        ObjectProvider<ChatClient> emptyClientProvider = emptyProvider();
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient builtClient = mock(ChatClient.class);
        when(builder.build()).thenReturn(builtClient);
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient.Builder> builderProvider = mock(ObjectProvider.class);
        when(builderProvider.getIfAvailable()).thenReturn(builder);

        ChatService chatService = configuration.chatService(environment, emptyClientProvider, builderProvider,
                mock(DocumentSearchService.class), new PromptFactory());

        assertThat(chatService).isInstanceOf(OpenAiChatService.class);
    }

    private <T> ObjectProvider<T> emptyProvider() {
        @SuppressWarnings("unchecked")
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }
}
