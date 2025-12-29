package com.example.wx.config.knowledge;

import com.example.wx.enums.KnowledgeBaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangx
 * @description 工厂模式实现知识库创建
 * @create 2025/8/7 21:18
 */
@Component
public class KnowledgeBaseFactory {

    private final Map<KnowledgeBaseType, KnowledgeBaseService> knowledgeBaseServiceMap = new HashMap<>();

    public KnowledgeBaseFactory(List<KnowledgeBaseService> services) {
        for (KnowledgeBaseService service : services) {
            knowledgeBaseServiceMap.put(service.getType(), service);
        }
    }

    @Value("${spring.ai.knowledge-base.primary-type:pgvector}")
    private String primaryType;

    @Value("${spring.ai.knowledge-base.fallback-type:simple}")
    private String fallbackType;

    /**
     * 获取知识库服务（支持降级）
     */
    public KnowledgeBaseService getKnowledgeBaseService() {
        KnowledgeBaseType primary = KnowledgeBaseType.valueOf(primaryType.toUpperCase());
        KnowledgeBaseService primaryService = knowledgeBaseServiceMap.get(primary);

        if (primaryService != null && primaryService.isAvailable()) {
            return primaryService;
        }

        // 降级到备用知识库
        KnowledgeBaseType fallback = KnowledgeBaseType.valueOf(fallbackType.toUpperCase());
        return knowledgeBaseServiceMap.get(fallback);
    }
}