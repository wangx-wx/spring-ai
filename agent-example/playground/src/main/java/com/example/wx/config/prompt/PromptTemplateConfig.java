package com.example.wx.config.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:27
 */
@Configuration
public class PromptTemplateConfig {
    @Bean("transformerPromptTemplate")
    public PromptTemplate transformerPromptTemplate() {

        return new PromptTemplate(
                """
                Given a user query, rewrite the user question to provide better results when querying {target}.
                
                You should follow these rules:
                
                1. Remove any irrelevant information and make sure the query is concise and specific;
                2. The output must be consistent with the language of the user's query;
                3. Ensure better understanding and answers from the perspective of large models.
                
                Original query:
                {query}
                
                Query after rewrite:
                """
        );
    }
}
