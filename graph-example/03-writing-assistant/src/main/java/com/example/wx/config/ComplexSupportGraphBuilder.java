package com.example.wx.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author wangxiang
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
        // todo 节点
        return stateGraph.compile();
    }

}
