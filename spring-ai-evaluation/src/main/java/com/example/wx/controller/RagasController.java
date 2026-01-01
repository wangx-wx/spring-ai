package com.example.wx.controller;

import com.example.wx.evaluator.CustomerEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wang.x
 * @description
 * @create 2025/12/31 15:10
 */
@RestController
@RequestMapping("/ai/evaluation")
@Slf4j
public class RagasController {


    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;
    private final Advisor ragAdvisor;
    private final Advisor loggerAdvisor;
    private final ChatClient chatClient;
    private final ChatClient.Builder chatClientBuilder;

    public RagasController(ObjectMapper objectMapper, EmbeddingModel embeddingModel, ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
        this.chatClient = chatClientBuilder.defaultSystem("中文作答").build();
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        this.objectMapper = objectMapper;
        this.ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5d)
                        .topK(3)
                        .build())
                .build();
        this.loggerAdvisor = SimpleLoggerAdvisor.builder().build();
    }

    @PostConstruct
    public void init() {
        var searchDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .filterExpression(new FilterExpressionBuilder().eq("title", "中国的首都").build())
                        .similarityThreshold(0.5d)
                        .topK(3)
                        .build()
        );
        if (CollectionUtils.isEmpty(searchDocuments)) {
            var content = """
                    中华人民共和国首都位于北京市，中华人民共和国成立前夕的旧称为北平，
                    是中共中央及中央人民政府所在地，中央四个直辖市之一，
                    全国政治、文化、国际交往和科技创新中心，中国古都、国家历史文化名城和国家中心城市之一。
                    """;
            var document = new Document(content, Map.of("title", "中国的首都"));
            vectorStore.add(List.of(document));
        }
    }

    /**
     * 相关性评估器, 用来评估AI生成的响应与提供的上下文的相关性. 该评估器通过确定AI模型的响应是否与用户关于检索到的上下文的输入相关来帮助评估RAG流的质量.
     */
    @GetMapping("/relevancy")
    public String relevancy(@RequestParam(value = "query", defaultValue = "中国的首都是哪里?") String query) {
        var ragChatResponse = ragChat(query);
        var context = ragChatResponse.documents();
        var response = ragChatResponse.response();

        var evaluator = RelevancyEvaluator.builder().chatClientBuilder(chatClientBuilder).build();
        var evaluationRequest = new EvaluationRequest(
                // Query
                query,
                // Context
                context,
                // Response
                response
        );
        var pass = evaluate(evaluator, evaluationRequest);

        return pass ? response : "暂无数据";
    }

    @GetMapping("/custom")
    public String custom(@RequestParam(value = "query", defaultValue = "你可以做什么?") String query) {
        var ragChatResponse = ragChat(query);
        var context = ragChatResponse.documents();
        var response = ragChatResponse.response();

        var evaluator = new CustomerEvaluator(chatClientBuilder, objectMapper);
        var evaluationRequest = new EvaluationRequest(
                query, context, response
        );
        var pass = evaluate(evaluator, evaluationRequest);

        return pass ? response : "暂无数据";
    }


    private RagChatResponse ragChat(String query) {
        var chatResponse = chatClient
                .prompt()
                .advisors(ragAdvisor, loggerAdvisor)
                .user(query)
                .call()
                .chatResponse();
        final List<Document> documents = Optional.ofNullable(chatResponse).orElseThrow().getMetadata()
                .get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        var response = chatResponse.getResult().getOutput().getText();
        return new RagChatResponse(query, documents, response);
    }

    private boolean evaluate(Evaluator evaluator, EvaluationRequest evaluationRequest) {
        var evaluationResponse = evaluator.evaluate(evaluationRequest);
        log.debug("AI模型评估响应: {}", evaluationResponse);
        var pass = evaluationResponse.isPass();
        log.info("AI模型评估结果: {}", pass);
        return pass;
    }

    private record RagChatResponse(String query, List<Document> documents, String response) {

    }

}
