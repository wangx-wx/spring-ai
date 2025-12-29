package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.example.wx.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangx
 * @description
 * @create 2025/6/22 14:10
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
        // deepseek
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public ChatClient dashScopeChatClient(DashScopeChatModel chatModel) {
        // dashscope
        return ChatClient.create(chatModel);
    }
}
