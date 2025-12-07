package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.dto.Result;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/7 21:41
 */
public class InterruptableNodeAction implements AsyncNodeActionWithConfig, InterruptableAction {

    private final String nodeId;
    private final String message;

    public InterruptableNodeAction(String nodeId, String message) {
        this.nodeId = nodeId;
        this.message = message;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        // 正常节点逻辑：更新状态
        return CompletableFuture.completedFuture(Map.of("messages", message));
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 检查是否需要中断
        // 如果状态中没有 human_feedback，则中断等待用户输入
        var slotParams = state.value("slot_params", Result.class).get();

        if ("1".equals(slotParams.status())) {
            // 返回 InterruptionMetadata 来中断执行
            InterruptionMetadata interruption = InterruptionMetadata.builder(nodeId, state)
                    .addMetadata("message", slotParams.reply())
                    .addMetadata("node", nodeId)
                    .build();
            return Optional.of(interruption);
        }
        // 如果已经有 human_feedback，继续执行
        return Optional.empty();
    }
}
