package com.example.wx.service;

import com.example.wx.entiy.tools.ToolCallResp;

import java.io.IOException;
import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/8/25 21:23
 */
public interface McpService {
    ToolCallResp chat(String prompt);

    ToolCallResp run(String id, Map<String, String> env, String prompt) throws IOException;
}
