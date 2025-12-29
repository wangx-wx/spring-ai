package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author wangx
 * @description
 * @create 2025/5/26 14:08
 */
@Component
public class MilvusConfig {

    // @Bean
    // public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
    //     return MilvusVectorStore.builder(milvusClient, embeddingModel)
    //             .collectionName("vector_store")
    //             .databaseName("spring_test")
    //             .indexType(IndexType.IVF_FLAT)
    //             .metricType(MetricType.COSINE)
    //             .batchingStrategy(new TokenCountBatchingStrategy())
    //             .initializeSchema(true)
    //             .build();
    // }

    // @Bean
    // public MilvusServiceClient milvusClient() {
    //     return new MilvusServiceClient(ConnectParam.newBuilder()
    //             .withUri(milvusContainer.getEndpoint())
    //             .build());
    // }

    // @Bean
    // public EmbeddingModel embeddingModel(DashScopeApi dashScopeApi) {
    //     return new DashScopeEmbeddingModel(dashScopeApi);
    // }
    //
    // @Bean
    // public DashScopeApi dashScopeApi() {
    //     return new DashScopeApi(System.getenv("DASH_SCOPE_API_KEY"));
    // }


}
