package com.ai.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

    //private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    public void ingestHandbook(Resource handbookResource) {
        log.info("Loading handbook data...");

        TextReader textReader = new TextReader(handbookResource);
        textReader.getCustomMetadata().put("filename", "handbook.txt");
        List<Document> documents = textReader.get();

        log.info("Splitting documents...");
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> splitDocuments = splitter.apply(documents);

        log.info("Adding {} documents to vector store...", splitDocuments.size());
        vectorStore.add(splitDocuments);
        log.info("Handbook loaded successfully.");
    }

    public String generateAnswer(String question) {
        log.info("Retrieving relevant documents for question: {}", question);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(3));

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        String promptText = """
                Context information is below.
                ---------------------
                {context}
                ---------------------
                Given the context information and no prior knowledge, just give the answer to the question asked: {question}
                .If you don't know the answer, just say that you don't know
                """;

        log.info("prompt: {}", promptText);

        return chatClient.prompt()
                .user(u -> u.text(promptText)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();
    }

}
