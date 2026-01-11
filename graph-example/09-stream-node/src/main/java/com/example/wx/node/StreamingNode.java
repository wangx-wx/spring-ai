package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 流式节点实现 - 使用 GraphFluxGenerator 处理流式响应
 */
public class StreamingNode implements NodeAction {

    private final ChatClient chatClient;
    private final String nodeId;

    public StreamingNode(ChatClient.Builder chatClientBuilder, String nodeId) {
        this.chatClient = chatClientBuilder.build();
        this.nodeId = nodeId;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String query = (String) state.value("query").orElse("");

        // 获取流式响应
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                .user(query)
                .stream()
                .chatResponse();

        // 将流式响应存储在状态中
        return Map.of("messages1", chatResponseFlux);
    }
}