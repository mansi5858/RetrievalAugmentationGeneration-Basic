package com.ai.rag.controller;

import com.ai.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagService ragService;

    @Test
    void uploadHandbook_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/upload"))
                .andExpect(status().isOk())
                .andExpect(content().string("File Uploaded Successfully"));

        verify(ragService).ingestHandbook(any(Resource.class));
    }

    @Test
    void generateAnswer_ShouldReturnAnswer() throws Exception {
        String question = "What is the meaning of life?";
        String answer = "42";

        when(ragService.generateAnswer(question)).thenReturn(answer);

        mockMvc.perform(get("/ai")
                        .param("question", question))
                .andExpect(status().isOk())
                .andExpect(content().string(answer));

        verify(ragService).generateAnswer(question);
    }
}
