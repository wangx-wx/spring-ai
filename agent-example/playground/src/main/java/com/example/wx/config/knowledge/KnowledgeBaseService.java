package com.example.wx.config.knowledge;

import com.example.wx.enums.KnowledgeBaseType;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * @author wangx
 * @description 知识库服务接口
 * @create 2025/8/7 21:15
 */
public interface KnowledgeBaseService {
    /**
     * 创建知识库检索顾问
     */
    BaseAdvisor createRetrievalAdvisor();

    /**
     * 获取知识库类型
     */
    KnowledgeBaseType getType();

    /**
     * 检查知识库是否可用
     */
    boolean isAvailable();
}
