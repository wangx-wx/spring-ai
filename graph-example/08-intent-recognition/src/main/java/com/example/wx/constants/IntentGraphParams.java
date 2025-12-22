package com.example.wx.constants;

import java.util.Map;

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

    // 意图所覆盖范围
    String INTENT_DESC = "intent_desc";

    // 语义召回结果
    String INTENT_RAG_RESULT = "intent_rag_list";
    String QA_RAG_RESULT = "qa_rag_list";

    // 槽位提取所需信息
    // 当前时间
    String NOW_DATE = "now_date";
    // 周几
    String WEEK_DAY = "now_day";
    // 第几周
    String WEEK_OF_YEAR = "week_of_year";
    String CLARIFY_LIST = "clarify_list";

    String SKIP_ASSESS_FLAG = "skip_assess";
    String ASSESS_RESULT = "assess_result";

    String OUTPUT_SCHEMA_KEY = "output_schema_key";

    String NODE_ID = "node_id";

    // 回复变量
    String REPLY = "reply";
    // 恢复执行
    String RESUME = "resume";

    String AGENT_TOOL_INPUT = "agent_tool_inputkey";
    String AGENT_TOOL_OUTPUT = "agent_tool_outputkey";
}
