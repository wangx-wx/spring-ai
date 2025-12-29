package com.example.wx.service.impl;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.example.wx.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:01
 */
@Service
public class BailianRagServiceImpl implements RagService {
    private final ChatClient client;

    private final DashScopeApi dashscopeApi;

    @Value("${spring.ai.alibaba.playground.bailian.index-name:default-index}")
    private String indexName;

    public BailianRagServiceImpl(
            DashScopeApi dashscopeApi,
            SimpleLoggerAdvisor simpleLoggerAdvisor,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor,
            @Qualifier("dashscopeChatModel") ChatModel chatModel,
            @Qualifier("systemPromptTemplate") PromptTemplate systemPromptTemplate
    ) {

        this.dashscopeApi = dashscopeApi;
        this.client = ChatClient.builder(chatModel)
                .defaultSystem(
                        systemPromptTemplate.getTemplate()
                ).defaultAdvisors(
                        messageChatMemoryAdvisor,
                        simpleLoggerAdvisor
                ).build();
    }

    @Override
    public Flux<String> ragChat(String chatId, String prompt) {
        return client.prompt()
                .user(prompt)
                .advisors(memoryAdvisor -> memoryAdvisor
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                ).advisors(
                        new DocumentRetrievalAdvisor(
                                new DashScopeDocumentRetriever(
                                        dashscopeApi,
                                        DashScopeDocumentRetrieverOptions.builder()
                                                .withIndexName(indexName)
                                                .build()
                                )
                        )
                ).stream()
                .content();
    }
}
