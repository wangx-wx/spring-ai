package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.Map;

/**
 * 处理流式输出的节点 - 接收并处理流式响应
 */
public class ProcessStreamingNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) {
        // 请注意，虽然上一个节点返回的是Flux对象，但是在引擎运行到当前节点时，
        // 框架已经完成了对上一个节点Flux对象的自动订阅与消费，并将最终的结果汇总后添加到了 messages key 中（基于 AppendStrategy）
        Object messages = state.value("messages1").orElse("");
        String result = "流式响应已处理完成: " + messages;
        return Map.of("result", result);
    }
}