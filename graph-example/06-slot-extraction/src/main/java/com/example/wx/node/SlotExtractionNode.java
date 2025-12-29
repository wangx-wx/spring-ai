package com.example.wx.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.example.wx.dto.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangx
 * @description
 * @create 2025/12/13 21:42
 */
public class SlotExtractionNode implements AsyncNodeActionWithConfig, InterruptableAction {
    private final ChatModel chatModel;
    private static final String SLOT_RESULT_KEY = "_slot_extraction_result_";

    public SlotExtractionNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
        // ✅ 从 context 中获取 interrupt() 的提取结果
        Result slotParams = (Result) config.context().remove(SLOT_RESULT_KEY);

        if (slotParams == null) {
            // 理论上不会发生，作为保护重新提取
            slotParams = doExtractSlots(state);
        }

        return CompletableFuture.completedFuture(Map.of("slot_params", slotParams));
    }

    @Override
    public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
        // ✅ 1. 进行槽位提取
        Result slotParams = doExtractSlots(state);

        // ✅ 2. 把结果放入 context，供 apply() 使用
        config.context().put(SLOT_RESULT_KEY, slotParams);
        state.input(Map.of("slot_params", slotParams));

        // 3. 检查是否需要中断
        if ("1".equals(slotParams.status())) {
            return Optional.of(InterruptionMetadata.builder(nodeId, state)
                    .addMetadata("message", slotParams.reply())
                    .addMetadata("current_slots", slotParams.slots())
                    .build());
        }

        // 4. 槽位完整，不中断，继续执行 apply()
        return Optional.empty();
    }

    private Result doExtractSlots(OverAllState state) {
        // 实际的槽位提取逻辑（调用 LLM）
        String userQuery = state.value("user_query", "");
        String nowDate = state.value("nowDate", "2025-12-13");
        String nowDay = state.value("nowDay", "星期六");
        String history = state.value("history", "");

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().build())
                .defaultSystem(SYSTEM_PROMPT)
                .build();

        return chatClient.prompt()
                .user(userQuery)
                .system(s -> s.params(Map.of(
                        "nowDate", nowDate,
                        "nowDay", nowDay,
                        "history", history
                )))
                .call()
                .entity(Result.class);
    }

    private static final String SYSTEM_PROMPT = """
            # Role
            你是一个槽位提取助手，从用户输入中提取时间范围和邮箱信息。如果参数丢失向用户发出澄清判断
            ## Skills
            1. **时间语义解析**
               - 自然语言时间识别：准确识别“上个月”“近一周”“去年五一”等口语化表达
               - 节假日别称映射：将“国庆”“春节”等俗称映射至官方放假区间
               - 模糊时间消歧：根据上下文和当前时间推断最合理的日期范围
               - 区间标准化：统一输出为起止日期格式（YYYY-MM-DD 至 YYYY-MM-DD）
            
            2. **日历逻辑计算**
               - 完整周计算：以周一为始、周日为终的周划分逻辑
               - 月份边界处理：正确处理“上个月”在1月时应为前一年12月
               - 滚动周期计算：“近一个月”按日减法而非固定月份数调整
               - 法定节假日融合判断：支持合并假期（如2025中秋+国庆）
            
            ## Rules
            1. **基本原则**：
               - 输出必须基于给定的当前时间上下文进行推导
               - 所有时间表达必须转换为标准的起止日期区间
               - 若输入包含多个时间片段，需分别解析并返回列表
               - 不得引入外部节假日知识，仅使用提供的 Holiday Reference
            
            2. **行为准则**：
               - “上周”指当前周之前的一个完整周（星期一至星期日）
               - “近一周”定义为 [当前日期 - 7天, 当前日期 - 1天] 的连续七天
               - “近一个月”为 [当前日期 - 1月, 当前日期 - 1天]，逐日倒推
               - “上个月”指当前月份的上一个完整自然月（如4月则取3月全月）
               - “最近一个节假日”指严格早于当前时间的最近一个法定假期区间
               - 年份表达如“2024年”代表该年全年区间：2024-01-01 至 2024-12-31
            
            3. **限制条件**：
               - 不处理未来节假日（除非明确提及且存在于参考表中）
            
            ## System Context
            - **当前时间**: {nowDate}
            - **今天星期**: {nowDay}
            
            ## Holiday Reference（2024-2025）
            ### 2024年
            - 元旦：2023-12-30 至 2024-01-01
            - 春节：2024-02-10 至 2024-02-17
            - 清明节：2024-04-04 至 2024-04-06
            - 劳动节：2024-05-01 至 2024-05-05
            - 端午节：2024-06-08 至 2024-06-10
            - 中秋节：2024-09-15 至 2024-09-17
            - 国庆节：2024-10-01 至 2024-10-07
            
            ### 2025年
            - 元旦：2025-01-01 至 2025-01-01
            - 春节：2025-01-28 至 2025-02-04
            - 清明节：2025-04-04 至 2025-04-06
            - 劳动节：2025-05-01 至 2025-05-05
            - 端午节：2025-05-31 至 2025-06-02
            - 中秋/国庆（合并）：2025-10-01 至 2025-10-08
            
            ## Workflows
            - 目标: 将自然语言中的时间描述转化为标准化的时间区间
            - 步骤 1: 解析输入文本，识别关键词（如“上周”“五一”“近一个月”）
            - 步骤 2: 根据 Rules 和 System Context 进行时间推算
            - 步骤 3: 查找 Holiday Reference 表完成节假日映射
            ## Chat Memory
            {history}
            
            ## Few-shot
            1. 示例1：
              - 用户输入: “上周”
              - 说明: 假设当前时间为 2024-04-05（周五），上周应为 2024-03-25 至 2024-03-31，但是缺少邮箱
              - 输出内容: "status":"1","reply":"请补充邮箱","slots":"startDate":"2024-03-25","endDate":"2024-03-31","email":""
            
            2. 示例2：
              - 用户输入: “123@qwe.com”
              - 说明: 邮箱为123@qwe.com
              - 输出内容: "status":"1","reply":"请补充时间范围","slots":"startDate":"","endDate":"","email":"123@qwe.com"
            
            3. 示例3：
              - 用户输入: “国庆期间的123@qwe.com”
              - 说明: 假设当前时间为 2025-11-05，国庆期间对应的时间范围 2024-03-25 至 2024-03-31
              - 输出内容: "status":"1","reply":"请补充时间范围","slots":"startDate":"2024-03-25","endDate":"2024-03-31","email":"123@qwe.com"
            """;
}
