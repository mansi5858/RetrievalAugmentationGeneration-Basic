package com.ai.rag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private VectorStore vectorStore;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        ragService = new RagService(chatClientBuilder, vectorStore);
    }

    @Test
    void ingestHandbook_ShouldAddDocumentsToVectorStore() {
        String content = "This is a test handbook content.";
        Resource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));

        ragService.ingestHandbook(resource);

        verify(vectorStore, times(1)).add(any(List.class));
    }

    @Test
    void generateAnswer_ShouldReturnAnswer() {
        String question = "What is this?";
        String answer = "This is a test.";
        Document document = new Document("This is a test handbook content.");

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));
        
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(java.util.function.Consumer.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(answer);

        String result = ragService.generateAnswer(question);

        assertEquals(answer, result);
        verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
        verify(chatClient, times(1)).prompt();
    }
}
