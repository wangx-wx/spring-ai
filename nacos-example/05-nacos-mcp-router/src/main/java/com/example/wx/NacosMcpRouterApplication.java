package com.example.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nacos MCP Router 示例应用
 * <p>
 * 该应用提供 MCP 服务的发现、向量存储和智能路由功能：
 * <ul>
 *     <li>从 Nacos 注册中心发现 MCP Server</li>
 *     <li>将服务信息向量化存储，支持语义搜索</li>
 *     <li>根据任务描述智能路由到合适的 MCP Server</li>
 *     <li>提供 REST API 进行服务管理和查询</li>
 * </ul>
 * </p>
 *
 * @author wangx
 */
@SpringBootApplication
public class NacosMcpRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosMcpRouterApplication.class, args);
    }
}
