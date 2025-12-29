package com.example.wx.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 10:53
 */
public class ProgrammaticTools {
    public String getTime(@ToolParam(description = "时间格式") String format) {
        System.out.println("format = " + format);
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).format((DateTimeFormatter.ofPattern(format)));
    }
}
