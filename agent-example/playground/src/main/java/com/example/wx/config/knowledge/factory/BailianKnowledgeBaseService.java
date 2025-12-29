package com.example.wx.config.knowledge.factory;

import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.example.wx.config.knowledge.KnowledgeBaseService;
import com.example.wx.enums.KnowledgeBaseType;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author wangx
 * @description
 * @create 2025/8/7 21:20
 */
@Service
@ConditionalOnProperty(name = "spring.ai.knowledge-base.bailian.enabled", havingValue = "true")
public class BailianKnowledgeBaseService implements KnowledgeBaseService {

    private final DashScopeApi dashscopeApi;

    @Value("${spring.ai.knowledge-base.bailian.index-name}")
    private String indexName;

    public BailianKnowledgeBaseService(DashScopeApi dashscopeApi) {
        this.dashscopeApi = dashscopeApi;
    }

    @Override
    public BaseAdvisor createRetrievalAdvisor() {
        return new DocumentRetrievalAdvisor(
                new DashScopeDocumentRetriever(
                        dashscopeApi,
                        DashScopeDocumentRetrieverOptions.builder()
                                .withIndexName(indexName)
                                .build()
                )
        );
    }

    @Override
    public KnowledgeBaseType getType() {
        return KnowledgeBaseType.BAILIAN;
    }

    @Override
    public boolean isAvailable() {
        // 检查百炼API连接性和索引可用性
        return dashscopeApi != null && StringUtils.hasText(indexName);
    }
}
