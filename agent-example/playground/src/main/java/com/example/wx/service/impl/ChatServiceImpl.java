package com.example.wx.service.impl;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.wx.advisor.ReasoningContentAdvisor;
import com.example.wx.config.knowledge.KnowledgeBaseFactory;
import com.example.wx.enums.ModelEnums;
import com.example.wx.exceptions.AppException;
import com.example.wx.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:29
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final Map<String, ChatClient> chatClientMap;

    private final PromptTemplate deepThinkPromptTemplate;

    private final ReasoningContentAdvisor reasoningContentAdvisor;

    private final BaseAdvisor ragAdvisor;

    public ChatServiceImpl(
            @Qualifier("deepThinkPromptTemplate") PromptTemplate deepThinkPromptTemplate,
            Map<String, ChatClient> chatClientMap,
            KnowledgeBaseFactory knowledgeBaseFactory
    ) {
        this.chatClientMap = chatClientMap;
        this.deepThinkPromptTemplate = deepThinkPromptTemplate;
        this.reasoningContentAdvisor = new ReasoningContentAdvisor(1);
        this.ragAdvisor = knowledgeBaseFactory.getKnowledgeBaseService().createRetrievalAdvisor();
    }

    @Override
    public Flux<String> chat(String chatId, String model, String prompt) {
        ChatClient chatClient = getChatClient(model);
        log.debug("chat model is: {}", model);
        var runtimeOptions = DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(0.8)
                .withResponseFormat(DashScopeResponseFormat.builder()
                        .type(DashScopeResponseFormat.Type.TEXT)
                        .build()
                ).build();
        return chatClient.prompt()
                .options(runtimeOptions)
                .user(prompt)
                .advisors(memoryAdvisor -> memoryAdvisor
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .advisors(ragAdvisor)
                .stream()
                .content();
    }

    @Override
    public Flux<String> deepThinkingChat(String chatId, String model, String prompt) {
        ChatClient chatClient = getChatClient(ModelEnums.DEEPSEEK_REASONER.getName());
        return chatClient.prompt()
                .options(DeepSeekChatOptions.builder()
                        .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER)
                        .temperature(0.8)
                        .responseFormat(
                                ResponseFormat.builder()
                                        .type(ResponseFormat.Type.TEXT).build())
                        .build()
                ).system(deepThinkPromptTemplate.getTemplate())
                .user(prompt)
                .advisors(ragAdvisor)
                .advisors(memoryAdvisor -> memoryAdvisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream().content();
    }

    private ChatClient getChatClient(String model) {
        ModelEnums modelEnums = ModelEnums.getModelByType(model);
        if (modelEnums == null) {
            throw new AppException("Input model not support.");
        }
        ChatClient chatClient = chatClientMap.get(modelEnums.getClient());
        if (modelEnums == ModelEnums.DEEPSEEK_REASONER) {
            chatClient.prompt().advisors(reasoningContentAdvisor);
        }
        return chatClient;
    }
}
