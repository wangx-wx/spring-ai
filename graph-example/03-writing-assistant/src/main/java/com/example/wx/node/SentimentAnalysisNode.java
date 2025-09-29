package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:48
 */
public class SentimentAnalysisNode implements NodeAction {

    private final ChatClient chatClient;

    private final String key;

    public SentimentAnalysisNode(ChatClient client, String key) {
        this.chatClient = client;
        this.key = key;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String text = (String) state.value(key).orElse("");
        ChatResponse resp = chatClient.prompt().user("emotion analysis from: " + text).call().chatResponse();
        String sentiment = resp.getResult().getOutput().getText();
        return Map.of("sentiment", sentiment);
    }
}
