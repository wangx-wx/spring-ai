package com.example.wx.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

/**
 * @author wangx
 * @description
 * @create 2025/12/26 16:46
 */
public class CustomStopConditionHook extends ModelHook {
    @Override
    public String getName() {
        return "custom_stop_condition";
    }
    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        // 检查是否找到答案，展示使用 OverAllState
        boolean answerFound = (Boolean) state.value("answer_found").orElse(false);
        // 检查错误次数，展示使用 RunnableConfig
        int errorCount = (Integer) config.context().getOrDefault("error_count", 0);

        // 找到答案或错误过多时停止
        if (answerFound || errorCount > 3) {
            List<Message> messages = new ArrayList<>();
            messages.add(new AssistantMessage(
                    answerFound ? "已找到答案，Agent 执行完成。"
                            : "错误次数过多 (" + errorCount + ")，Agent 执行终止。"
            ));
            // the messages will be appended to the original message list context.
            return CompletableFuture.completedFuture(Map.of("messages", messages));
        }

        return CompletableFuture.completedFuture(Map.of());
    }

}
