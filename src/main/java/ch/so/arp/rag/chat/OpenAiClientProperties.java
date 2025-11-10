package ch.so.arp.rag.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Simple configuration properties describing how to connect to OpenAI.
 */
@ConfigurationProperties(prefix = "rag.chat.openai")
public class OpenAiClientProperties implements EnvironmentAware {

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

    private Environment environment;

    public String getApiKey() {
        if (StringUtils.hasText(apiKey)) {
            return apiKey;
        }
        return environment != null ? environment.getProperty("spring.ai.openai.api-key") : null;
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

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
