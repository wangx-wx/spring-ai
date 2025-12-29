package com.example.wx.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.AnswerNode;
import com.alibaba.cloud.ai.graph.node.DocumentExtractorNode;
import com.alibaba.cloud.ai.graph.node.HttpNode;
import com.alibaba.cloud.ai.graph.node.HumanNode;
import com.alibaba.cloud.ai.graph.node.KnowledgeRetrievalNode;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.node.ParameterParsingNode;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.node.ToolNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangx
 * @description
 * @create 2025/9/29 18:25
 */
@Component
public class ComplexSupportGraphBuilder {

    @Bean
    public CompiledGraph buildGraph(ChatModel chatModel, VectorStore vectorStore, ToolCallbackResolver toolCallbackResolver)
            throws GraphStateException {
        // ChatClient
        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            for (String key : List.of("input", "attachments", "docs", "parameterParsing_output", "classifier_output"
                    , "retrieved_docs", "filtered_docs", "http_response", "llm_response", "tool_result", "hum_feedback", "answer")) {
                keyStrategyHashMap.put(key, new ReplaceStrategy());
            }
            return keyStrategyHashMap;
        };

        StateGraph stateGraph = new StateGraph(keyStrategyFactory);

        // 1. Document extraction
        DocumentExtractorNode extractorNode = DocumentExtractorNode.builder()
                .fileList(List.of("data/manual.txt"))
                .paramsKey("attachments")
                .outputKey("docs")
                .build();

        stateGraph.addNode("extractDocs", node_async(extractorNode));

        // 2.Parameter parsing
        ParameterParsingNode paramNode = ParameterParsingNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .parameters(List.of(new ParameterParsingNode.Param("ticketId", "string", "工单编号"),
                        new ParameterParsingNode.Param("priority", "string", "优先级")))
                .build();
        stateGraph.addNode("parseParams", node_async(paramNode));

        // 3. Classification of issues
        QuestionClassifierNode qcNode = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("input")
                .categories(List.of("售后", "技术支持", "投诉", "咨询"))
                .classificationInstructions(List.of("请仅返回最合适的类别名称String类型，例如：售后、运输、产品质量、其他；不要多余的标记或格式。 正确返回结果： 售后 "))
                .build();
        stateGraph.addNode("classify", node_async(qcNode));

        // 4. Knowledge Retrieval
        KnowledgeRetrievalNode krNode = KnowledgeRetrievalNode.builder()
                .userPromptKey("classifier_output")
                .vectorStore(vectorStore)
                .topK(5)
                .similarityThreshold(0.5)
                .enableRanker(false)
                .build();
        stateGraph.addNode("retrieveDocs", node_async(krNode));

        // 6. call http endpoint
        HttpNode httpNode = HttpNode.builder()
                .webClient(WebClient.builder().build())
                .method(HttpMethod.GET)
                .url("http://localhost:10024/api/graph/mock/http?" + "ticketId=12345" + "&category=售后")
                .outputKey("http_response")
                .build();
        stateGraph.addNode("syncTicket", node_async(httpNode));

        // 7. call llm
        LlmNode llmNode = LlmNode.builder()
                .chatClient(chatClient)
                .systemPromptTemplate("你是客服助手，请基于以下信息撰写回复：")
                .userPromptTemplateKey("http_response")
                .messagesKey("user_prompt")
                .outputKey("llm_response")
                .build();
        stateGraph.addNode("invokeLLM", node_async(llmNode));

        // 8. Perform a tool call (optional)
        ToolNode toolNode = ToolNode.builder()
                .llmResponseKey("llm_response")
                .outputKey("tool_result")
                .toolCallbackResolver(toolCallbackResolver)
                .toolNames(List.of("sendEmail", "updateCRM"))
                .build();
        stateGraph.addNode("invokeTool", node_async(toolNode));

        // 9. human callback
        HumanNode humanNode = new HumanNode("conditioned",
                st -> st.value("tool_result").map(r -> r.toString().contains("ERROR")).orElse(false),
                st -> Map.of("answer", st.value("tool_result").orElse("").toString()));
        stateGraph.addNode("humanReview", node_async(humanNode));

        // 10. end print
        AnswerNode answerNode = AnswerNode.builder()
                .answer("{{answer}}")
                .build();
        stateGraph.addNode("finalAnswer", node_async(answerNode));

        stateGraph.addEdge(StateGraph.START, "extractDocs")
                .addEdge("extractDocs", "parseParams")
                .addEdge("parseParams", "classify")
                .addEdge("classify", "retrieveDocs")
                .addEdge("retrieveDocs", "syncTicket")
                .addEdge("syncTicket", "invokeLLM")
                .addEdge("invokeLLM", "invokeTool")
                .addEdge("invokeTool", "humanReview")
                .addEdge("humanReview", "finalAnswer")
                .addEdge("finalAnswer", StateGraph.END);
        return stateGraph.compile();
    }

}
