package com.example.wx.config.node;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.wx.domain.LLMConfig;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/9 22:06
 */
@AllArgsConstructor
public class LLMNode implements NodeAction {

    private final ChatModel chatModel;
    private final String systemPrompt;
    private final LLMConfig llmConfig;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultOptions(buildOptions())
                .defaultSystem(systemPrompt)
                .build();
        String query = state.value(llmConfig.getQueryKey(), "");
        Optional<ChatResponse> chatResponse = Optional.ofNullable(chatClient.prompt().user(query).call().chatResponse());
        Map<String, Object> result = new HashMap<>();
        String answer = chatResponse.map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElse(null);
        result.put(llmConfig.getOutputKey(), answer);

        if (llmConfig.isIncludeMetadata()) {
            result.put("tokens", chatResponse.map(ChatResponse::getMetadata)
                    .map(ChatResponseMetadata::getUsage)
                    .map(Usage::getTotalTokens)
                    .orElse(null));
            result.put("model", chatResponse.map(ChatResponse::getMetadata).map(ChatResponseMetadata::getModel).orElse(null));
        }
        return result;
    }

    private DashScopeChatOptions buildOptions() {
        return DashScopeChatOptions.builder()
                .withModel(llmConfig.getModel())
                .withTemperature(llmConfig.getTemperature())
                .withTopP(llmConfig.getTopP())
                .withMaxToken(llmConfig.getMaxTokens())
                .build();
    }

}
