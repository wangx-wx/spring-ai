package com.example.wx;

import com.example.wx.tools.DemoTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wangxiang
 * @description
 * @create 2025/7/5 23:38
 */
@SpringBootApplication
public class NacosMcpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosMcpServerApplication.class, args);
    }

    @Bean
    public DemoTool demoTool() {
        return new DemoTool();
    }

    @Bean
    public ToolCallbackProvider serverTools(DemoTool demoTool) {
        return ToolCallbackProvider.from(ToolCallbacks.from(demoTool));
    }
}