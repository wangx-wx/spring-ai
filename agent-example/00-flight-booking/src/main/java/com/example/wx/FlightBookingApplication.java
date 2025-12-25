package com.example.wx;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.Set;

/**
 * @author wangxiang
 * @description
 * @create 2025/6/4 14:50
 */
@SpringBootApplication
public class FlightBookingApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(FlightBookingApplication.class, args);
    }

    private static final Logger logger = LoggerFactory.getLogger(FlightBookingApplication.class);

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(
            VectorStore vectorStore,
            @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs
    ) {

        return args -> {
            // Ingest the document into the vector store
            /*
             * 1、文档读取TextReader 读取 resources/rag/terms-of-service.txt 文件内容
             * 2、TokenTextSplitter 按token长度切分文本（避免大文本超出模型限制）
             * 3、向量化存储 通过 VectorStore.write() 将文本向量存入内存（后续可用于RAG检索）
             */
            vectorStore.write(new TokenTextSplitter().transform(new TextReader(termsOfServiceDocs).read()));

            // 相似性搜索检测
            vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
                logger.info("Similar Document: {}", doc.getText());
            });
        };
    }

    /**
     * 提供基于内存的向量存储（SimpleVectorStore）
     * <p>
     * 依赖 EmbeddingModel（自动注入，Alibaba的嵌入模型）
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 存储多轮对话历史（基于内存）
     * 实现上下文感知的连续对话
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    /**
     * 提供可自定义的HTTP客户端（用于调用外部API）
     * @return
     */
    // @Bean
    // @ConditionalOnMissingBean
    // public RestClient.Builder restClientBuilder() {
    //     return RestClient.builder();
    // }
}