package com.example.wx.service;

import com.example.wx.entiy.tools.ToolCallResp;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:05
 */
public interface ToolsService {
    ToolCallResp chat(String prompt);
}
