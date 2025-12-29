package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/10/25 17:19
 */
@SpringBootApplication
public class GraphObservabilityApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("MODEL_SCOPE_API_KEY", "MODEL_SCOPE_BASE_URL", "DASH_SCOPE_API_KEY"
                , "MODEL_SCOPE_MODEL", "YOUR_BASE64_ENCODED_CREDENTIALS");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(GraphObservabilityApplication.class, args);
    }
}