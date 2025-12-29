package com.example.wx.compontent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 18:04
 */
public class DateTimeTools {
    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).format((DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }

    public static void main(String[] args) {
        new DateTimeTools().setAlarm("2020-01-02T10:15:30");
        System.out.println("new DateTimeTools().getCurrentDateTime() = " + new DateTimeTools().getCurrentDateTime());
    }
}
