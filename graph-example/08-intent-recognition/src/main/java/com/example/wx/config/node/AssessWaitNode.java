package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.domain.tool.AssessResult;
import com.example.wx.domain.tool.SlotFillingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.example.wx.constants.IntentGraphParams.RESUME;

@RequiredArgsConstructor
public class AssessWaitNode implements AsyncNodeActionWithConfig, InterruptableAction {
    private final String inputKey;
    private final String outputKey;

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        config.context().remove(RESUME);
        return CompletableFuture.completedFuture(Collections.emptyMap());
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 获取是否是恢复会话
        boolean isResume = (boolean) config.context().getOrDefault(RESUME, false);
        var assessResult = state.value(inputKey, AssessResult.class).orElse(AssessResult.empty());

        if (StringUtils.hasText(assessResult.reply())) {
            state.input(Map.of(outputKey, assessResult.reply()));
        }
        if (isResume || "2".equals(assessResult.status())) {
            return Optional.empty();
        }
        return Optional.of(InterruptionMetadata.builder(nodeId, state)
                .addMetadata(outputKey, assessResult.reply())
                .build());
    }
}