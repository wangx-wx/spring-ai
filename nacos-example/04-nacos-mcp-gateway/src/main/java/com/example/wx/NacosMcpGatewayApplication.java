package com.example.wx;

import com.alibaba.cloud.ai.mcp.gateway.nacos.callback.NacosMcpGatewayToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nacos MCP Gateway 示例应用
 * <p>
 * 该应用作为 MCP Gateway 网关，从 Nacos 注册中心发现并聚合多个 MCP Server 的工具，
 * 并将这些工具统一暴露给 MCP Client 或 AI 模型调用。
 * </p>
 *
 * @author wangx
 */
@SpringBootApplication
public class NacosMcpGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosMcpGatewayApplication.class, args);
    }
}
