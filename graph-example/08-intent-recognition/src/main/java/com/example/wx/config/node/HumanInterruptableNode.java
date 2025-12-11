package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.domain.tool.SlotFillingResult;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.example.wx.constants.IntentGraphParams.SLOT_PARAMS;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/11 22:00
 */
@RequiredArgsConstructor
public class HumanInterruptableNode implements AsyncNodeActionWithConfig, InterruptableAction {

    private final String nodeId;

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 检查是否需要中断
        var slotParams = state.value(SLOT_PARAMS, SlotFillingResult.class)
                .orElseThrow(() -> new IllegalStateException("slot_params is missing"));

        // 如果需要补充信息
        if ("1".equals(slotParams.status())) {
            // 返回 InterruptionMetadata 来中断执行
            InterruptionMetadata interruption = InterruptionMetadata.builder(nodeId, state)
                    .addMetadata("message", slotParams.reply())
                    .addMetadata("node", nodeId)
                    .build();
            return Optional.of(interruption);
        }
        // 继续执行
        return Optional.empty();
    }
}
