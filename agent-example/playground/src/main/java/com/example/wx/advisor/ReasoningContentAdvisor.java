package com.example.wx.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:20
 */
public class ReasoningContentAdvisor implements BaseAdvisor {

    private final static Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

    private final int order;

    public ReasoningContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    @Override
    public int getOrder() {

        return this.order;
    }

    @NotNull
    @Override
    public ChatClientRequest before(@NotNull ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(@NotNull ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        ChatResponse resp = chatClientResponse.chatResponse();

        if (Objects.isNull(resp)) {
            return chatClientResponse;
        }

        // 注释，避免冗长的日志打印
        logger.debug("Advisor metadata output: {}", resp.getResults().get(0).getOutput().getMetadata());
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));
        logger.debug("Advisor reasoning content: {}", reasoningContent);
        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                    .map(generation -> {
                        AssistantMessage output = generation.getOutput();
                        AssistantMessage thinkAssistantMessage = new AssistantMessage(
                                String.format("<think>%s</think>", reasoningContent) + output.getText(),
                                output.getMetadata(),
                                output.getToolCalls(),
                                output.getMedia());
                        return new Generation(thinkAssistantMessage, generation.getMetadata());
                    }).toList();
            ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
            return ChatClientResponse.builder().context(chatClientResponse.context()).chatResponse(thinkChatResp).build();
        }
        return chatClientResponse;
    }
}
