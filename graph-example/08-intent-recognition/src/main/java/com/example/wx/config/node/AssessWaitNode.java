package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.domain.tool.AgentToolResult;
import com.example.wx.domain.tool.AssessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.converter.BeanOutputConverter;
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
    private final BeanOutputConverter<?> converter;

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
        AssessResult assessResult = AssessResult.empty();
        Optional<Object> value = state.value(inputKey);
        if (value.isPresent()) {
            Object object = value.get();
            if (object instanceof AssistantMessage assistantMessage) {
                assessResult = (AssessResult) converter.convert(Objects.requireNonNull(assistantMessage.getText()));
            } else {
                assessResult = (AssessResult) object;
            }
        } else {
            throw new RuntimeException(inputKey + "is null");
        }

        boolean isResume = (boolean) config.context().getOrDefault(RESUME, false);

        if (StringUtils.hasText(assessResult.reply())) {
            state.input(Map.of(outputKey, assessResult.reply()));
        }
        if ("2".equals(assessResult.status())) {
            return Optional.empty();
        }
        if (isResume) {
            String userQuery = state.value(USER_QUERY, "true");
            if (Objects.equals(userQuery, "true")) {
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