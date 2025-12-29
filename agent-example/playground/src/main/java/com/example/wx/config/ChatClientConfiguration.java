package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangx
 * @description
 * @create 2025/7/27 21:24
 */
@Configuration
public class ChatClientConfiguration {

    @Bean("deepseekChatClient")
    public ChatClient deepseekChatClient(
            SimpleLoggerAdvisor simpleLoggerAdvisor,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor,
            @Qualifier("systemPromptTemplate") PromptTemplate systemPromptTemplate,
            DeepSeekChatModel chatModel) {
        // deepseek
        return ChatClient.builder(chatModel)
                .defaultSystem(
                        systemPromptTemplate.getTemplate()
                ).defaultAdvisors(
                        simpleLoggerAdvisor,
                        messageChatMemoryAdvisor
                )
                .build();
    }

    @Bean("dashscopeChatClient")
    public ChatClient dashscopeChatClient(
            SimpleLoggerAdvisor simpleLoggerAdvisor,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor,
            @Qualifier("systemPromptTemplate") PromptTemplate systemPromptTemplate,
            DashScopeChatModel chatModel) {
        // dashscope
        return ChatClient.builder(chatModel)
                // .defaultSystem(systemPromptTemplate.getTemplate())
                .defaultAdvisors(
                        simpleLoggerAdvisor,
                        messageChatMemoryAdvisor
                )
                .build();
    }
}
