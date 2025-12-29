package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 16:47
 */
@SpringBootApplication
public class PromptApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DEEPSEEK_API_KEY", "DEEPSEEK_BASE_URL");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(PromptApplication.class, args);
    }
}