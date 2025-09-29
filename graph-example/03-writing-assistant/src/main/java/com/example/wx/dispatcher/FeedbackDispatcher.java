package com.example.wx.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:28
 */
public class FeedbackDispatcher implements EdgeAction {

    @Override
    public String apply(OverAllState state) throws Exception {
        String feedback = (String) state.value("summary_feedback").orElse("");
        if (feedback.contains("positive")) {
            return "positive";
        }
        return "negative";
    }
}
