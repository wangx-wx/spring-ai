package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:46
 */
public class InputNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String inputText = (String) state.value("inputText").orElse("");
        return Map.of("inputText", inputText);
    }
}
