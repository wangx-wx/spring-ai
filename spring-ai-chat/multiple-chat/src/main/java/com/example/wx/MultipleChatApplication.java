package com.example.wx;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * @author wangx
 * @description
 * @create 2025/6/22 13:56
 */
@SpringBootApplication
public class MultipleChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleChatApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(@Qualifier("deepseekChatClient") ChatClient deepseekChatClient,
                                        @Qualifier("dashScopeChatClient") ChatClient dashScopeChatClient) {
        return args -> {
            var scanner = new Scanner(System.in);
            ChatClient chat;

            // Model selection
            System.out.println("\nSelect your AI model:");
            System.out.println("1. Deepseek");
            System.out.println("2. DashScope");
            System.out.print("Enter your choice (1 or 2): ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                chat = deepseekChatClient;
                System.out.println("Using Deepseek model");
            } else {
                chat = dashScopeChatClient;
                System.out.println("Using DashScope model");
            }

            // Use the selected chat client
            System.out.print("\nEnter your question: ");
            String input = scanner.nextLine();
            String response = chat.prompt(input).call().content();
            System.out.println("ASSISTANT: " + response);

            scanner.close();
        };
    }
}