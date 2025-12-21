package com.example.wx;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.example.wx.domain.RagDoc;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;
import org.springframework.core.io.Resource;

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


//     @Bean
//     CommandLineRunner test(ChatModel chatModel, DashScopeDocumentRetriever dashScopeDocumentRetriever, @Value("classpath:/prompts/rewrite_prompt.st") Resource systemResource) {
//         return args -> {
// //            List<Document> retrieve = dashScopeDocumentRetriever.retrieve(new Query("12个月的账单"));
// //            ArrayList<RagDoc> docs = new ArrayList<>(retrieve.size());
// //            retrieve.stream()
// //                    .map(d -> new RagDoc(d.getId(), d.getText(), (String) d.getMetadata().get("doc_name"), (Double) d.getMetadata().get("_score")))
// //                    .forEach(docs::add);
// //            System.out.println("docs = " + docs);
//             ChatClient chatClient = ChatClient.builder(chatModel)
//                     .defaultSystem(systemResource)
//                     .build();
//             String content = chatClient.prompt()
//                     .system(s -> s.params(Map.of("history", "[]"))).user("12个月的账单")
//                     .call().content();
//             System.out.println("content = " + content);
//         };
//
//     }
}