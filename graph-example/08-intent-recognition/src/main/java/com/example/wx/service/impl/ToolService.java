package com.example.wx.service.impl;

import com.example.wx.domain.ToolResult;
import com.example.wx.domain.tool.DownloadMerchantIncomeRequest;
import com.example.wx.domain.tool.MerchantOrderIncomeTimeRequest;
import com.networknt.org.apache.commons.validator.routines.EmailValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author wangx
 * @description
 * @create 2025/12/11 21:54
 */
@Component("toolService")
@Slf4j
public class ToolService {

    public ToolResult allAnalyse(MerchantOrderIncomeTimeRequest request, ToolContext toolContext) {
        log.info("分析工具入参：{}", request);
        return ToolResult.success("allAnalyse", "总收入1000元");
    }

    public ToolResult downloadTool(DownloadMerchantIncomeRequest request, ToolContext toolContext) {
        log.info("下载工具入参：{}", request);
        if (!StringUtils.hasText(request.getEmail())) {
            return ToolResult.error("downloadTool", "请输入邮箱地址");
        }

        if (!EmailValidator.getInstance().isValid(request.getEmail())) {
            return ToolResult.error("downloadTool", "邮箱地址格式错误");
        }
        return ToolResult.success("allAnalyse", "下载成功，2分钟查看");
    }
}
