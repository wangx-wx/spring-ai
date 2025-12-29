package com.example.wx.controller;

import com.example.wx.annotation.UserIp;
import com.example.wx.entiy.result.Result;
import com.example.wx.entiy.tools.ToolCallResp;
import com.example.wx.exceptions.AppException;
import com.example.wx.service.McpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/8/25 21:22
 */
@RestController
@Tag(name = "MCP APIs")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class McpController {
    private final McpService mcpService;


    /**
     * 内部接口不应该直接被 web 请求！
     */
    @UserIp
    @GetMapping("/inner/mcp")
    @Operation(summary = "DashScope MCP Chat")
    public Result<ToolCallResp> mcpChat(
            @Validated @RequestParam("prompt") String prompt
    ) {
        return Result.success(mcpService.chat(prompt));
    }

    @UserIp
    @PostMapping("/mcp-run")
    @Operation(summary = "MCP Run")
    public Result<ToolCallResp> mcpRun(
            @Validated @RequestParam("id") String id,
            @Validated @RequestParam("prompt") String prompt,
            @RequestParam(value = "envs", required = false) String envs
    ) {

        Map<String, String> env = new HashMap<>();
        if (StringUtils.hasText(envs)) {
            for (String entry : envs.split(",")) {
                String[] keyValue = entry.split("=");
                if (keyValue.length == 2) {
                    env.put(keyValue[0], keyValue[1]);
                }
            }
        }

        try {
            return Result.success(mcpService.run(id, env, prompt));
        } catch (IOException e) {
            throw new AppException(e.getMessage());
        }
    }
}
