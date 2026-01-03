package com.example.wx;

import com.example.wx.tools.OpenMeteoService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wangx
 * @description
 * @create 2026/1/3 17:54
 */
@SpringBootApplication
public class NacosMcpServerStreamableApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosMcpServerStreamableApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider serverTools(OpenMeteoService openMeteoService) {
        return MethodToolCallbackProvider.builder().toolObjects(openMeteoService).build();
    }
}