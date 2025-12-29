package com.example.wx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wangx
 * @description
 * @create 2025/8/7 21:22
 */
@ConfigurationProperties(prefix = "spring.ai.knowledge-base")
@Data
public class KnowledgeBaseProperties {
    private String primaryType = "pgvector";
    private String fallbackType = "simple";
    private BailianProperties bailian = new BailianProperties();
    private PgVectorProperties pgvector = new PgVectorProperties();
    private SimpleProperties simple = new SimpleProperties();

    @Data
    public static class BailianProperties {
        private boolean enabled = true;
        private String indexName = "default-index";
    }

    @Data
    public static class PgVectorProperties {
        private boolean enabled = true;
        private int topK = 5;
        private double similarityThreshold = 0.7;
    }

    @Data
    public static class SimpleProperties {
        private boolean enabled = true;
    }
}
