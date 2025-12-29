package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/15 21:20
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ChatMemoryApplication {
    public static void main(String[] args) {
        // 加载 .env 文件中的 key-value 到环境变量
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(ChatMemoryApplication.class, args);
    }
}