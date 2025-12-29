package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.Set;

/**
 * @author wangxiang
 * @description
 * @create 2025/7/12 21:01
 */
@SpringBootApplication
public class NacosMcpClientApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DEEPSEEK_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(NacosMcpClientApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder, @Qualifier("loadbalancedMcpAsyncToolCallbacks") ToolCallbackProvider tools,
                                               ConfigurableApplicationContext context) {
        return args -> {
            var chatClient = chatClientBuilder
                    .defaultToolCallbacks(tools.getToolCallbacks())
                    .build();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n>>> QUESTION: ");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                if (userInput.isEmpty()) {
                    userInput = "北京时间现在几点钟";
                }
                System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
            }
            scanner.close();
            context.close();
        };
    }
    // @Bean
    // public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
    //     return args -> {
    //         var chatClient = chatClientBuilder
    //                 .build();
    //
    //         Scanner scanner = new Scanner(System.in);
    //         while (true) {
    //             System.out.print("\n>>> QUESTION: ");
    //             String userInput = scanner.nextLine();
    //             if (userInput.equalsIgnoreCase("exit")) {
    //                 break;
    //             }
    //             if (userInput.isEmpty()) {
    //                 userInput = "北京时间现在几点钟";
    //             }
    //             System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    //         }
    //         scanner.close();
    //     };
    // }

}