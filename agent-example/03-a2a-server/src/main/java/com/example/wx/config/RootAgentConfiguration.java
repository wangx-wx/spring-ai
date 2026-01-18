package com.example.wx.config;

import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RootAgentConfiguration {

    private static final String SYSTEM_PROMPT =
            """
                    You are an assistant specializing in Spring AI Alibaba. Your role is to provide accurate, helpful, and context-aware support for any questions related to Spring AI Alibaba, including its features, integration, configuration, usage patterns, and best practices.
                    If a question is not related to Spring AI Alibaba, please politely decline to answer with a brief apology.
                    When addressing questions about Spring AI Alibaba, leverage available tools to retrieve up-to-date documentation, analyze configurations, or validate code examples. If no suitable tools are available, rely on your internal knowledge to provide a clear and informative response.
                    Always aim to assist developers in effectively using Spring AI Alibaba within their applications.
                    """;

    @Bean
    @Primary
    public Agent rootAgent(ChatModel chatModel) throws GraphStateException {
        return ReactAgent.builder()
                .name("SaaAgent")
                .description(
                        "Answer question about Spring AI Alibaba and do some maintain and query operation about Spring AI Alibaba by chinese.")
                .model(chatModel)
                .instruction(SYSTEM_PROMPT)
                .outputKey("messages")
                .build();
    }
}