package com.example.wx.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/28 22:12
 */
public class HumanFeedbackDispatcher implements EdgeAction {
    @Override
    public String apply(OverAllState state) {
        return state.value("human_next_node", StateGraph.END);
    }
}
