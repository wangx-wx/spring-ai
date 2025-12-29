package com.example.wx.config.knowledge.factory;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.model.RerankModel;
import com.example.wx.config.knowledge.KnowledgeBaseService;
import com.example.wx.config.rag.VectorStoreDelegate;
import com.example.wx.enums.KnowledgeBaseType;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author wangx
 * @description
 * @create 2025/8/7 21:21
 */
@Service
@ConditionalOnProperty(name = "spring.ai.knowledge-base.pgvector.enabled", havingValue = "true")
public class PgVectorKnowledgeBaseService implements KnowledgeBaseService {

    private final VectorStoreDelegate vectorStoreDelegate;
    private final RerankModel rerankModel;

    @Value("${spring.ai.knowledge-base.pgvector.top-k:5}")
    private int topK;

    public PgVectorKnowledgeBaseService(VectorStoreDelegate vectorStoreDelegate, RerankModel rerankModel) {
        this.vectorStoreDelegate = vectorStoreDelegate;
        this.rerankModel = rerankModel;
    }

    @Override
    public BaseAdvisor createRetrievalAdvisor() {
        return new RetrievalRerankAdvisor(
                vectorStoreDelegate.getVectorStore("pgvector"),
                rerankModel,
                SearchRequest.builder().topK(topK).build()
        );
    }

    @Override
    public KnowledgeBaseType getType() {
        return KnowledgeBaseType.PGVECTOR;
    }

    @Override
    public boolean isAvailable() {
        return vectorStoreDelegate.getVectorStore("pgvector") != null;
    }
}

