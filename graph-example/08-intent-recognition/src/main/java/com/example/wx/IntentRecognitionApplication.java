package com.example.wx;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/9 21:30
 */
@SpringBootApplication
public class IntentRecognitionApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(IntentRecognitionApplication.class, args);
    }


    @Bean
    CommandLineRunner test(ChatModel chatModel, DashScopeDocumentRetriever dashScopeDocumentRetriever) {
        return args -> {
            List<Document> list = dashScopeDocumentRetriever.retrieve(new Query("上个月"));
            System.out.println("list = " + list);
        };

    }
}