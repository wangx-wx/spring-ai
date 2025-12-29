package com.example.wx.config;

import com.example.wx.tools.DemoTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 21:09
 */
@Configuration
public class McpServerConfig {
    @Bean
    public DemoTool demoTool() {
        return new DemoTool();
    }

    @Bean
    public ToolCallbackProvider serverTools(DemoTool demoTool) {
        return ToolCallbackProvider.from(ToolCallbacks.from(demoTool));
        // return MethodToolCallbackProvider.builder().toolObjects(demoTool).build();
    }
}
