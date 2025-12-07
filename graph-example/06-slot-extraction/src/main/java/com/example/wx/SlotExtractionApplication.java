package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Set;

/**
 *@description 
 *@author wangxiang
 *@create 2025/12/7 18:40
 */
@SpringBootApplication
public class SlotExtractionApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(SlotExtractionApplication.class, args);
    }
}