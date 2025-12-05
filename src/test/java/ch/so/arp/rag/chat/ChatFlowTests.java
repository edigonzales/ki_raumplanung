package ch.so.arp.rag.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ChatFlowTests {

    @Autowired
    private MockMvc mockMvc;

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
    void summaryPanelRendersSseConnect() throws Exception {
        mockMvc.perform(post("/summary")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("prompt", "Bitte zusammenfassen")
                        .param("sectionIds", "1")
                        .param("documentIds", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sse-connect")))
                .andExpect(content().string(not(containsString("Bitte wähle mindestens einen Abschnitt"))));
    }

    @Test
    void streamSummaryEmitsEvents() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chat/summary-stream")
                        .param("prompt", "Test")
                        .param("sectionIds", "1", "2")
                        .param("documentIds", "11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        result.getAsyncResult(2000L);

        MvcResult dispatched = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/event-stream")))
                .andReturn();

        String body = dispatched.getResponse().getContentAsString();
        assertThat(body).contains("data:").contains("Zusammenfassung");
    }

    @Test
    void streamSummaryHandlesEmptyIdentifiers() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/chat/summary-stream")
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
