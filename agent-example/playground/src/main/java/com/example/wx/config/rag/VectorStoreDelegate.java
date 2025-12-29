package com.example.wx.config.rag;

import org.springframework.ai.vectorstore.VectorStore;

import java.util.Objects;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:57
 */
public class VectorStoreDelegate {

    private final VectorStore simpleVectorStore;

    private final VectorStore analyticdbVectorStore;

    private final VectorStore pgVectorStore;

    public VectorStoreDelegate(VectorStore simpleVectorStore, VectorStore analyticdbVectorStore, VectorStore pgVectorStore) {
        this.simpleVectorStore = simpleVectorStore;
        this.analyticdbVectorStore = analyticdbVectorStore;
        this.pgVectorStore = pgVectorStore;
    }

    public VectorStore getVectorStore(String vectorStoreType) {

        if (Objects.equals(vectorStoreType, "analyticdb") && analyticdbVectorStore != null) {
            return analyticdbVectorStore;
        }

        if (Objects.equals(vectorStoreType, "pgvector") && pgVectorStore != null) {
            return pgVectorStore;
        }

        return simpleVectorStore;
    }
}

