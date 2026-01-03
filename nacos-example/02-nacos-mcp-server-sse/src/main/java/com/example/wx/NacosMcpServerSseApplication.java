package com.example.wx;

import com.example.wx.tools.TimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wangx
 * @description
 * @create 2025/7/5 23:38
 */
@SpringBootApplication
public class NacosMcpServerSseApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosMcpServerSseApplication.class, args);
    }

    @Bean
    public TimeTool demoTool() {
        return new TimeTool();
    }

    @Bean
    public ToolCallbackProvider serverTools(TimeTool demoTool) {
        return MethodToolCallbackProvider.builder().toolObjects(demoTool).build();
    }
}