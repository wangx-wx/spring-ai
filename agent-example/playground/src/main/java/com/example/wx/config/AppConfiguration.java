package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.memory.jdbc.PostgresChatMemoryRepository;
import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemoryRepository;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author wangx
 * @description 全局统一管理 ChatMemory Bean 和 SimpleLoggerAdvisor
 * @create 2025/7/20 17:07
 */
@Configuration
public class AppConfiguration {

    private static final String DASH_SCOPE_API_KEY_PREFIX = "DASH_SCOPE_API_KEY";

    @Bean
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor(100);
    }
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(
            ChatMemory pgsqlChatMemory
    ) {
        return MessageChatMemoryAdvisor.builder(pgsqlChatMemory).build();
    }

    // @Bean
    // public ChatMemory SQLiteChatMemory(JdbcTemplate jdbcTemplate) {
    //     return MessageWindowChatMemory.builder()
    //             .chatMemoryRepository(SQLiteChatMemoryRepository.sqliteBuilder()
    //                     .jdbcTemplate(jdbcTemplate)
    //                     .build())
    //             .build();
    // }

    @Bean
    public ChatMemory PgsqlChatMemory(JdbcTemplate jdbcTemplate) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(PostgresChatMemoryRepository.postgresBuilder()
                        .jdbcTemplate(jdbcTemplate)
                        .build()).build();
    }

    @Bean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(System.getProperty(DASH_SCOPE_API_KEY_PREFIX))
                .build();
    }
}
