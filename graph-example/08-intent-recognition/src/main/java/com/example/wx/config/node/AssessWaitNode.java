package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.domain.tool.AssessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.example.wx.constants.IntentGraphParams.RESUME;
import static com.example.wx.constants.IntentGraphParams.USER_QUERY;

@RequiredArgsConstructor
public class AssessWaitNode implements AsyncNodeActionWithConfig, InterruptableAction {
    private final String inputKey;
    private final String outputKey;

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        config.context().remove(RESUME);
        AssessResult updateAssessResult = (AssessResult) config.context().remove(inputKey);
        Map<String, Object> updates = new HashMap<>();
        if (updateAssessResult != null && Objects.equals(updateAssessResult.status(), "2")) {
            updates.put(inputKey, updateAssessResult);
        }
        return CompletableFuture.completedFuture(updates);
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // 获取是否是恢复会话
        var assessResult = state.value(inputKey, AssessResult.class).orElse(AssessResult.empty());
        boolean isResume = (boolean) config.context().getOrDefault(RESUME, false);

        if (StringUtils.hasText(assessResult.reply())) {
            state.input(Map.of(outputKey, assessResult.reply()));
        }
        if ("2".equals(assessResult.status())) {
            return Optional.empty();
        }
        if (isResume) {
            String value = state.value(USER_QUERY, "true");
            if (Objects.equals(value, "true")) {
                var updateAssessResult = new AssessResult(assessResult.confidence(), "2", assessResult.reply());
                config.context().put(inputKey, updateAssessResult);
            }
            return Optional.empty();
        }
        return Optional.of(InterruptionMetadata.builder(nodeId, state)
                .addMetadata(outputKey, assessResult.reply())
                .build());
    }
}