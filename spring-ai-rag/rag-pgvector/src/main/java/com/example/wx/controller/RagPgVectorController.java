package com.example.wx.controller;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.model.RerankModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2025/5/26 21:58
 */
@RestController
@RequestMapping("ai")
public class RagPgVectorController {
    @Value("classpath:/prompts/system-qa.st")
    private Resource systemResource;

    @Value("classpath:/data/spring_ai_alibaba_quickstart.pdf")
    private Resource springAiResource;

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final RerankModel rerankModel;

    public RagPgVectorController(VectorStore vectorStore, ChatModel chatModel, RerankModel rerankModel) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.rerankModel = rerankModel;
    }

    @GetMapping("/rag/import/docs")
    public void importDocument() {
        // 1. parse document
        DocumentReader reader = new PagePdfDocumentReader(springAiResource);
        List<Document> documents = reader.get();

        // 1.2 use local file
        // FileSystemResource fileSystemResource = new FileSystemResource("D:\\file.pdf");
        // DocumentReader reader = new PagePdfDocumentReader(fileSystemResource);

        // 2. split trunks
        List<Document> splitDocuments = new TokenTextSplitter().apply(documents);

        // 3. create embedding and store to vector store
        vectorStore.add(splitDocuments);
    }

    @GetMapping(value = "/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> generate(@RequestParam(value = "message",
            defaultValue = "how to get start with spring ai alibaba?") String message) throws IOException {
        SearchRequest searchRequest = SearchRequest.builder().topK(2).build();
        String promptTemplate = systemResource.getContentAsString(StandardCharsets.UTF_8);

        return ChatClient.builder(chatModel)
                .defaultAdvisors(new RetrievalRerankAdvisor(vectorStore, rerankModel, searchRequest, new SystemPromptTemplate(promptTemplate), 0.1))
                .build()
                .prompt()
                .user(message)
                .stream()
                .chatResponse();
    }
}
