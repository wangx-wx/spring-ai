package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.wx.tools.SearchTool;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/12/7 21:10
 */
public class AgentNode implements NodeAction {

    private final ChatClient chatClient;

    public AgentNode(ChatClient.Builder chatClientBuilder, SearchTool searchTool) {
        this.chatClient = chatClientBuilder
                .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // List<String> messages = state.value("messages", new TypeReference<List<String>>() {});
        //
        //
        // // 获取最后一条消息
        // String lastMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1);


        // 调用 LLM（会自动处理工具调用）
        String response = chatClient.prompt()
                .user("lastMessage")
                .call()
                .content();

        return Map.of("messages", response);
    }
}