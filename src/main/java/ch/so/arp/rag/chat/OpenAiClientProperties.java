package ch.so.arp.rag.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Simple configuration properties describing how to connect to OpenAI.
 */
@ConfigurationProperties(prefix = "rag.chat.openai")
public class OpenAiClientProperties {

    /**
     * API key that authorises requests against the OpenAI service.
     */
    private String apiKey;

    /**
     * Base URL for the API. Defaults to the public OpenAI endpoint.
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * Name of the chat model that should be used.
     */
    private String model = "gpt-4o-mini";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
