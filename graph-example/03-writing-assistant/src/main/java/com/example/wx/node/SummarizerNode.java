package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:19
 */
public class SummarizerNode implements NodeAction {

    private final ChatClient chatClient;

    public SummarizerNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String text = (String) state.value("original_text").orElse("");
        String prompt = "请对以下中文文本进行简洁明了的摘要：\n\n" + text;
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        String summary = chatResponse.getResult().getOutput().getText();
        return Map.of("summary", summary);
    }
}
