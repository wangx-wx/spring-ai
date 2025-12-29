package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 15:19
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PlayGroundApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("AI_DASH_SCOPE_API_KEY", "DEEPSEEK_API_KEY"
                , "VECTOR_STORE_TYPE", "BAIDU_TRANSLATE_APP_ID", "BAIDU_TRANSLATE_SECRET_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(PlayGroundApplication.class, args);
    }
}