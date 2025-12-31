package com.example.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangx
 * @description
 * @create 2025/5/21 15:44
 */
@SpringBootApplication
public class DashScopeChatModelApplication {
    public static void main(String[] args) {
        // 加载 .env 文件中的 key-value 到环境变量
//        Dotenv dotenv = Dotenv.configure().filename(".env").load();
//        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
//        for (String item : set) {
//            System.setProperty(item, dotenv.get(item));
//        }
        SpringApplication.run(DashScopeChatModelApplication.class, args);
    }
}