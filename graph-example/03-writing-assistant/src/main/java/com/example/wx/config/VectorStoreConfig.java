package com.example.wx.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 15:21
 */
@Configuration
public class VectorStoreConfig {

    @Value("${rag.source:classpath:data/manual.txt}")
    private Resource ragSource;


    @Bean
    @Primary
    public VectorStore customVectorStore(EmbeddingModel embeddingModel) {
        var chunks = new TokenTextSplitter().transform(new TextReader(ragSource).read());

        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.write(chunks);
        return vectorStore;
    }
}
