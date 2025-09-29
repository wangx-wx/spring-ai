package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 15:13
 */
@SpringBootApplication
public class WritingAssistantApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("MODEL_SCOPE_API_KEY", "MODEL_SCOPE_BASE_URL"
                , "MODEL_SCOPE_MODEL");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(WritingAssistantApplication.class, args);
    }
}