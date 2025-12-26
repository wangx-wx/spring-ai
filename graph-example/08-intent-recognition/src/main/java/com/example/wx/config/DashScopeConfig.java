package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.postgresql.PostgresSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/9 21:41
 */
@Configuration
public class DashScopeConfig {
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.intent-knowledge}")
    private String intentKnowledge;

    @Value("${spring.ai.dashscope.qa-knowledge}")
    private String qaKnowledge;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(apiKey).build();
    }

    @Bean
    public DashScopeDocumentRetriever intentKnowledgeRetriever(DashScopeApi dashScopeApi) {
        return new DashScopeDocumentRetriever(
                dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .indexName(intentKnowledge)
                        .rerankTopN(8)
                        .build()
        );
    }

    @Bean
    public DashScopeDocumentRetriever qaKnowledgeRetriever(DashScopeApi dashScopeApi) {
        return new DashScopeDocumentRetriever(
                dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .indexName(qaKnowledge)
                        .rerankTopN(8)
                        .build()
        );
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");

        return Redisson.create(config);
    }

    @Bean
    public RedisSaver redisSaver(RedissonClient redissonClient) {
        return RedisSaver.builder().redisson(redissonClient).build();
    }

    @Bean
    public MemorySaver postgresSaver() {
        return PostgresSaver.builder()
                .host("127.0.0.1")
                .port(5432)
                .database("db")
                .user("pgsql")
                .password("123456")
                .createTables(Boolean.TRUE)
                .build();
    }
}
