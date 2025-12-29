package com.example.wx.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 10:52
 */
public class DeclarativeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        System.out.println(" DeclarativeTools getCurrentDateTime() start");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).format((DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
