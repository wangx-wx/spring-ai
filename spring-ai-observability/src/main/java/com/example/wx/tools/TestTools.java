package com.example.wx.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * @author wangx
 * @description
 * @create 2025/6/18 22:16
 */
public class TestTools {
    @Tool(name = "getCurrentTime", description = "获取当前时间")
    public String getCurrentTime(){
        return "2025-06-19 15:56:06";
    }

}
