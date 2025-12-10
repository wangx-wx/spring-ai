package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
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

    @Value("${spring.ai.dashscope.index-name}")
    private String indexName;

    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(apiKey).build();
    }

    @Bean
    public DashScopeDocumentRetriever dashScopeDocumentRetriever(DashScopeApi dashScopeApi) {
        return new DashScopeDocumentRetriever(
                dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .indexName(indexName)
                        .rerankTopN(8)
                        .build()
        );
    }
}
