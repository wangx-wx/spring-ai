package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:53
 */
public class MergeResultsNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String sent = (String) state.value("sentiment").orElse("unknown");
        List<?> keywords = (List<?>) state.value("keywords").orElse(List.of());

        return Map.of("analysis", Map.of("sentiment", sent, "keywords", keywords));
    }
}
