package com.example.wx.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author wangx
 * @description KnowledgeBaseType
 * @create 2025/8/7 21:16
 */
@RequiredArgsConstructor
@Getter
public enum KnowledgeBaseType {
    BAILIAN("bailian", "百炼知识库"),
    PGVECTOR("pgvector", "PGVector向量库"),
    SIMPLE("simple", "内存向量库");

    private final String code;
    private final String description;
}
