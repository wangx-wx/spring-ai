package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.domain.tool.AgentToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.example.wx.constants.IntentGraphParams.REPLY;
import static com.example.wx.constants.IntentGraphParams.RESUME;

@RequiredArgsConstructor
public class AgentToolWaitNode implements AsyncNodeActionWithConfig, InterruptableAction {
    private final String inputKey;
    private final String outputKey;

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        config.context().remove(RESUME);
        var updateResult = state.value(inputKey, AgentToolResult.class).orElse(AgentToolResult.empty());
        ;
        Map<String, Object> updates = new HashMap<>();
        if (Objects.equals(updateResult.status(), "2")) {
            updates.put(REPLY, updateResult.reply());
        }
        return CompletableFuture.completedFuture(updates);
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 获取是否是恢复会话
        var agentToolResult = state.value(inputKey, AgentToolResult.class).orElse(AgentToolResult.empty());
        boolean isResume = (boolean) config.context().getOrDefault(RESUME, false);

        if (StringUtils.hasText(agentToolResult.reply())) {
            state.input(Map.of(outputKey, agentToolResult.reply()));
        }

        if (isResume || "2".equals(agentToolResult.status())) {
            return Optional.empty();
        }

        return Optional.of(InterruptionMetadata.builder(nodeId, state)
                .addMetadata(outputKey, agentToolResult.reply())
                .build());
    }
}