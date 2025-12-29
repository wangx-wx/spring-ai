package com.example.wx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangx
 * @description
 * @create 2025/8/24 17:46
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.tool-calling")
public class ToolCallingConfig {

    private Baidu baidu;

    @Data
    public static class Baidu {
        private Translate translate;
        private Map map;

        @Data
        public static class Translate {
            /**
             * 百度翻译 appId
             */
            private String ak;

            /**
             * 百度翻译 secretKey
             */
            private String sk;
        }

        @Data
        public static class Map {
            /**
             * 百度地图 API Key
             */
            private String ak;
        }
    }
}