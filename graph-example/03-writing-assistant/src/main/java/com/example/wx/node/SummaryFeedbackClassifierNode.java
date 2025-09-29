package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:22
 */
public class SummaryFeedbackClassifierNode implements NodeAction {

    private final ChatClient chatClient;

    private final String inputKey;

    public SummaryFeedbackClassifierNode(ChatClient chatClient, String inputKey) {
        this.chatClient = chatClient;
        this.inputKey = inputKey;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String summary = (String) state.value(inputKey).orElse("");
        if (!StringUtils.hasText(summary)) {
            throw new IllegalArgumentException("summary is empty in state");
        }

        String prompt = """
					以下是一个自动生成的中文摘要。请你判断它是否让用户满意。如果满意，请返回 "positive"，否则返回 "negative"：

					摘要内容：
					%s
					""".formatted(summary);

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String output = response.getResult().getOutput().getText();

        String classification = output.toLowerCase().contains("positive") ? "positive" : "negative";
        return Map.of("summary_feedback", classification);
    }
}
