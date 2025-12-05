package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TaskFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskContextStore contextStore;

    @Test
    void searchReturnsResults() throws Exception {
        mockMvc.perform(post("/search")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("keywords", "Hecke")
                        .param("lexicalWeight", "0.6"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gefundene Abschnitte")))
                .andExpect(content().string(containsString("Hecke")));
    }

    @Test
    void taskPanelRendersSseConnect() throws Exception {
        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("prompt", "Bitte beantworten")
                        .param("sectionIds", "1")
                        .param("useFullDocuments", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sse-connect")))
                .andExpect(content().string(not(containsString("Bitte wähle mindestens einen Abschnitt"))));
    }

    @Test
    void streamTaskEmitsEvents() throws Exception {
        var contextToken = contextStore.store(List.of(new SectionSelection(null, "file.pdf", "Titel", 1L, "Abschnitt 1", "Text")));

        MvcResult result = mockMvc.perform(get("/api/chat/task-stream")
                        .param("prompt", "Test")
                        .param("contextToken", contextToken.toString())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        result.getAsyncResult(2000L);

        MvcResult dispatched = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/event-stream")))
                .andReturn();

        String body = dispatched.getResponse().getContentAsString();
        assertThat(body).contains("data:").contains("Aufgabe");
    }

    @Test
    void streamTaskHandlesEmptyIdentifiers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chat/task-stream")
                        .param("prompt", "Leere Auswahl")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        result.getAsyncResult(2000L);

        MvcResult dispatched = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/event-stream")))
                .andReturn();

        String body = dispatched.getResponse().getContentAsString();
        assertThat(body).contains("Keine Abschnitte");
    }
}
