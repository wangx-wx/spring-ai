package com.example.wx.config.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallback;

import java.lang.reflect.Field;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:59
 */
public class SyncMcpToolCallbackWrapper {

    private final SyncMcpToolCallback callback;

    public SyncMcpToolCallbackWrapper(SyncMcpToolCallback callback) {
        this.callback = callback;
    }

    public McpSyncClient getMcpClient() {
        try {
            Field field = SyncMcpToolCallback.class.getDeclaredField("mcpClient");
            field.setAccessible(true);
            return (McpSyncClient) field.get(callback);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}