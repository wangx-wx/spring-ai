package com.example.wx.controller;

import com.alibaba.cloud.ai.mcp.router.model.response.McpServerAddResponse;
import com.alibaba.cloud.ai.mcp.router.model.response.McpServerSearchResponse;
import com.alibaba.cloud.ai.mcp.router.model.response.McpToolExecutionResponse;
import com.alibaba.cloud.ai.mcp.router.model.response.McpDebugResponse;
import com.alibaba.cloud.ai.mcp.router.service.McpRouterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP Router 测试控制器
 * <p>
 * 提供以下功能：
 * <ul>
 *     <li>语义搜索 MCP Server</li>
 *     <li>添加并初始化 MCP Server</li>
 *     <li>调用 MCP Server 的工具</li>
 *     <li>调试服务连接状态</li>
 *     <li>通过 AI 模型智能调用工具</li>
 * </ul>
 * </p>
 *
 * @author wangx
 */
@Slf4j
@RestController
@RequestMapping("/api/router")
@RequiredArgsConstructor
public class RouterController {

    private final McpRouterService mcpRouterService;
    private final ChatModel chatModel;

    /**
     * 语义搜索 MCP Server
     * <p>
     * 示例请求：
     * - GET /api/router/search?query=天气查询&limit=5
     * - GET /api/router/search?query=数据库操作&keywords=mysql,postgresql
     * </p>
     */
    @GetMapping("/search")
    public McpServerSearchResponse searchMcpServer(
            @RequestParam String query,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "5") Integer limit) {
        log.info("搜索 MCP Server: query={}, keywords={}, limit={}", query, keywords, limit);
        return mcpRouterService.searchMcpServer(query, keywords, limit);
    }

    /**
     * 添加并初始化 MCP Server
     * <p>
     * 示例请求：
     * - POST /api/router/servers/nacos-mcp-server-sse
     * </p>
     */
    @PostMapping("/servers/{serviceName}")
    public McpServerAddResponse addMcpServer(@PathVariable String serviceName) {
        log.info("添加 MCP Server: {}", serviceName);
        return mcpRouterService.addMcpServer(serviceName);
    }

    /**
     * 使用 MCP Server 的工具
     * <p>
     * 示例请求：
     * - POST /api/router/tools/call
     *   Body: { "serviceName": "nacos-mcp-server-sse", "toolName": "getCurrentTime", "parameters": "{}" }
     * </p>
     */
    @PostMapping("/tools/call")
    public McpToolExecutionResponse callTool(@RequestBody ToolCallRequest request) {
        log.info("调用工具: service={}, tool={}", request.serviceName(), request.toolName());
        return mcpRouterService.useTool(
                request.serviceName(),
                request.toolName(),
                request.parameters()
        );
    }

    /**
     * 调试服务连接状态
     * <p>
     * 示例请求：
     * - GET /api/router/debug/nacos-mcp-server-sse
     * </p>
     */
    @GetMapping("/debug/{serviceName}")
    public McpDebugResponse debugService(@PathVariable String serviceName) {
        log.info("调试服务连接: {}", serviceName);
        return mcpRouterService.debugMcpService(serviceName);
    }

    /**
     * 通过 AI 模型智能调用工具
     * <p>
     * 示例请求：
     * - GET /api/router/chat?message=帮我查询一下杭州的天气
     * </p>
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        log.info("收到用户消息: {}", message);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultTools(mcpRouterService)
                .build();

        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        log.info("AI 响应: {}", response);
        return response;
    }

    /**
     * 批量初始化服务
     * <p>
     * 示例请求：
     * - POST /api/router/initialize
     *   Body: ["nacos-mcp-server-sse", "nacos-mcp-server-streamable"]
     * </p>
     */
    @PostMapping("/initialize")
    public Map<String, McpServerAddResponse> initializeServers(@RequestBody List<String> serviceNames) {
        log.info("批量初始化服务: {}", serviceNames);
        return serviceNames.stream()
                .collect(java.util.stream.Collectors.toMap(
                        name -> name,
                        mcpRouterService::addMcpServer
                ));
    }

    /**
     * 工具调用请求
     */
    public record ToolCallRequest(
            String serviceName,
            String toolName,
            String parameters
    ) {}
}
