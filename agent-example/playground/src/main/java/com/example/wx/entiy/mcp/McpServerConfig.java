package com.example.wx.entiy.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 18:03
 */
public class McpServerConfig {

    @JsonProperty("mcpServers")
    private Map<String, McpStdioClientProperties.Parameters> mcpServers;

    public Map<String, McpStdioClientProperties.Parameters> getMcpServers() {
        return mcpServers;
    }

    public void setMcpServers(Map<String, McpStdioClientProperties.Parameters> mcpServers) {
        this.mcpServers = mcpServers;
    }

}