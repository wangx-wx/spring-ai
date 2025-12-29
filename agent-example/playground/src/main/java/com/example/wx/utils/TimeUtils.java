package com.example.wx.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:10
 */
public final class TimeUtils {

    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private TimeUtils() {
    }

    public static String getCurrentTime() {

        long currentTimeMillis = System.currentTimeMillis();

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        return dateTime.format(formatter);
    }
}