package com.example.wx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wangx
 * @description
 * @create 2025/12/9 21:30
 */
@SpringBootApplication
public class IntentRecognitionApplication {
    public static void main(String[] args) {
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