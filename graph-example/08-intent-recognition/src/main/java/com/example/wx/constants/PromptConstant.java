package com.example.wx.constants;

/**
 * @author wangx
 * @description
 * @create 2025/12/20 16:51
 */
public interface PromptConstant {
    String INTENT_NODE_USER_PROMPT = """
            <raw_query>{user_query}</raw_query>
            <rewrite_query>{rewrite_query}</rewrite_query>
            <recall_intents>{intent_rag_list}</recall_intents>
            """;
}
