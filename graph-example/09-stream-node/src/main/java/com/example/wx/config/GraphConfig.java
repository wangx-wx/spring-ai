package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.node.LLMNode;
import com.example.wx.node.ProcessStreamingNode;
import com.example.wx.node.StreamingNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * @author wangx
 * @description
 * @create 2026/1/11 15:30
 */
@Configuration
public class GraphConfig {

    @Bean
    public StateGraph streamGraph(ChatClient.Builder chatClientBuilder, ChatModel chatModel) throws GraphStateException {
        // 定义状态策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
            keyStrategyMap.put("query", new ReplaceStrategy());
            keyStrategyMap.put("messages1", new AppendStrategy());
            keyStrategyMap.put("result", new ReplaceStrategy());
            keyStrategyMap.put("outputKey", new ReplaceStrategy());
            return keyStrategyMap;
        };

        // 创建流式节点
        StreamingNode streamingNode = new StreamingNode(chatClientBuilder, "streaming_node");

        // 创建处理节点
        ProcessStreamingNode processNode = new ProcessStreamingNode();

        LLMNode llmNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .inputKey("query")
                .outputKey("outputKey")
                .build();
        LLMNode llmNode22 = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .inputKey("result")
                .outputKey("outputKey")
                .build();


        // 构建图
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("streaming_node", AsyncNodeAction.node_async(streamingNode))
                .addNode("process_node", AsyncNodeAction.node_async(processNode))
                .addNode("llm_node", AsyncNodeAction.node_async(llmNode))
                .addNode("llm_node2", AsyncNodeAction.node_async(llmNode22))
                .addEdge(START, "llm_node")
                .addEdge("llm_node", "streaming_node")
                .addEdge("streaming_node", "process_node")
                .addEdge("process_node", "llm_node2")
                .addEdge("llm_node2", END);
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "Intent Clarify Graph");
        System.out.println("======================================");
        System.out.println(representation.content());
        System.out.println("======================================");
        return stateGraph;
    }
}
