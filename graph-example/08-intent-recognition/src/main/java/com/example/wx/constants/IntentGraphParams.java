package com.example.wx.constants;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/10 18:39
 */
public interface IntentGraphParams {

    // 用户问题
    String USER_QUERY = "user_query";

    // 历史会话
    String HISTORY = "history_list";

    // 重写问题
    String REWRITE_QUERY = "rewrite_query";

    // 意图识别结果
    String INTENT_RESULT = "intent";

    // 语义召回结果
    String RAG_RESULT = "rag_list";

    // 槽位提取所需信息
    // 当前时间
    String NOW_DATE = "now_date";
    // 周几
    String WEEK_DAY = "now_day";
    // 第几周
    String WEEK_OF_YEAR = "week_of_year";
    String CLARIFY_LIST = "clarify_list";
    String SLOT_PARAMS = "slot_params";


    String NODE_ID = "node_id";
}
