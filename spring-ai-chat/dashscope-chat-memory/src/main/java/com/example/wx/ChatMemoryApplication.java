package com.example.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author wangx
 * @description
 * @create 2025/7/15 21:20
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ChatMemoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatMemoryApplication.class, args);
    }
}