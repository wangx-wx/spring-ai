package com.example.wx.config.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:35
 */
@Configuration
public class SimpleVectorStoreConfiguration {
    @Value("${spring.ai.alibaba.playground.bailian.enable:false}")
    private Boolean enable;

    /**
     * 提供基于内存的向量存储（SimpleVectorStore）
     * <p>
     * 依赖 EmbeddingModel（自动注入，Alibaba 的嵌入模型）
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate,EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
    }

    @Bean
    public VectorStoreDelegate vectorStoreDelegate(
            @Qualifier("simpleVectorStore") VectorStore simpleVectorStore,
            @Qualifier("pgVectorStore") VectorStore pgVectorStore,
            @Qualifier("analyticdbVectorStore") @Autowired(required = false) VectorStore analyticdbVectorStore
    ) {

        return new VectorStoreDelegate(simpleVectorStore, analyticdbVectorStore, pgVectorStore);
    }

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(VectorStoreDelegate vectorStoreDelegate) {
        return args -> {
            // 百炼知识库和向量存储初始化
            // 如果未启用百炼知识库，则默认用向量存储服务
            if (!enable) {
                String type = System.getProperty("VECTOR_STORE_TYPE");
                VectorStoreInitializer initializer = new VectorStoreInitializer();
                initializer.init(vectorStoreDelegate.getVectorStore(type));
            }
        };
    }
}
