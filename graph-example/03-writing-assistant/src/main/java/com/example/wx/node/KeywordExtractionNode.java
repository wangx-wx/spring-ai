package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:51
 */
public class KeywordExtractionNode implements NodeAction {

    private final ChatClient chatClient;
    private final String key;

    public KeywordExtractionNode(ChatClient chatClient, String key) {
        this.chatClient = chatClient;
        this.key = key;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String text = (String) state.value(key).orElse("");
        ChatResponse resp = chatClient.prompt().user("Extract keywords from: " + text).call().chatResponse();
        String kws = resp.getResult().getOutput().getText();
        return Map.of("keywords", List.of(kws.split(",\\s*")));
    }
}
