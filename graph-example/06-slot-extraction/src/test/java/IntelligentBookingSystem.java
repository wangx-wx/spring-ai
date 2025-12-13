import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeActionWithConfig;
import com.alibaba.cloud.ai.graph.action.InterruptableAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IntelligentBookingSystem {

    // ========== 槽位定义 ==========

    static class BookingSlots {
        public static final List<String> REQUIRED = List.of(
                "departure_city", "arrival_city", "departure_date"
        );

        public static final Map<String, String> SLOT_NAMES = Map.of(
                "departure_city", "出发城市",
                "arrival_city", "到达城市",
                "departure_date", "出发日期"
        );
    }

    // ========== 槽位提取节点 ==========

    static class SlotExtractionNode implements AsyncNodeActionWithConfig, InterruptableAction {

        private final ChatModel chatModel;

        public SlotExtractionNode(ChatModel chatModel) {
            this.chatModel = chatModel;
        }

        @Override
        public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
            String userInput = state.value("user_input", "").toString();
            Map<String, Object> currentSlots = (Map<String, Object>) state.value("slots")
                    .orElse(new HashMap<>());

            // 使用 LLM 提取槽位
            String extractionPrompt = buildExtractionPrompt(userInput, currentSlots);
            String llmResponse = chatModel.call(new Prompt(extractionPrompt))
                    .getResult().getOutput().getText();

            // 解析槽位
            Map<String, Object> extractedSlots = parseSlots(llmResponse);

            // 合并槽位
            Map<String, Object> mergedSlots = new HashMap<>(currentSlots);
            mergedSlots.putAll(extractedSlots);

            return CompletableFuture.completedFuture(Map.of("slots", mergedSlots));
        }

        @Override
        public Optional<InterruptionMetadata> interrupt(String nodeId, OverAllState state, RunnableConfig config) {
            // 检查是否是恢复执行
            if (state.value("human_feedback").isPresent()) {
                return Optional.empty();
            }

            // 检查槽位完整性
            Map<String, Object> slots = (Map<String, Object>) state.value("slots")
                    .orElse(new HashMap<>());

            List<String> missingSlots = BookingSlots.REQUIRED.stream()
                    .filter(slot -> !slots.containsKey(slot) ||
                            slots.get(slot) == null ||
                            slots.get(slot).toString().isEmpty())
                    .toList();

            if (!missingSlots.isEmpty()) {
                String prompt = generatePrompt(missingSlots, slots);

                return Optional.of(InterruptionMetadata.builder(nodeId, state)
                        .addMetadata("missing_slots", missingSlots)
                        .addMetadata("current_slots", slots)
                        .addMetadata("prompt", prompt)
                        .build());
            }

            return Optional.empty();
        }

        private String buildExtractionPrompt(String userInput, Map<String, Object> currentSlots) {
            return String.format("""
                    请从用户输入中提取订票信息，以JSON格式返回。
                    
                    当前已有信息：%s
                    用户输入：%s
                    
                    需要提取的字段：
                    - departure_city: 出发城市
                    - arrival_city: 到达城市
                    - departure_date: 出发日期（格式：YYYY-MM-DD）
                    
                    只返回JSON，不要其他说明。
                    示例：{"departure_city": "北京", "arrival_city": "上海", "departure_date": "2025-12-20"}
                    """, currentSlots, userInput);
        }

        private Map<String, Object> parseSlots(String llmResponse) {
            try {
                // 简单的 JSON 解析（实际应使用 JSON 库）
                Map<String, Object> slots = new HashMap<>();
                String json = llmResponse.trim();
                if (json.startsWith("{") && json.endsWith("}")) {
                    json = json.substring(1, json.length() - 1);
                    for (String pair : json.split(",")) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) {
                            String key = kv[0].trim().replaceAll("\"", "");
                            String value = kv[1].trim().replaceAll("\"", "");
                            slots.put(key, value);
                        }
                    }
                }
                return slots;
            } catch (Exception e) {
                return new HashMap<>();
            }
        }

        private String generatePrompt(List<String> missingSlots, Map<String, Object> currentSlots) {
            String missingInfo = missingSlots.stream()
                    .map(BookingSlots.SLOT_NAMES::get)
                    .reduce((a, b) -> a + "、" + b)
                    .orElse("");

            StringBuilder prompt = new StringBuilder();
            prompt.append("请提供以下信息：").append(missingInfo);

            if (!currentSlots.isEmpty()) {
                prompt.append("\n\n当前已收集到：");
                currentSlots.forEach((key, value) -> {
                    String name = BookingSlots.SLOT_NAMES.get(key);
                    if (name != null) {
                        prompt.append("\n- ").append(name).append(": ").append(value);
                    }
                });
            }

            return prompt.toString();
        }
    }

    // ========== 确认节点 ==========

    static class ConfirmationNode implements AsyncNodeActionWithConfig {

        @Override
        public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
            Map<String, Object> slots = (Map<String, Object>) state.value("slots").orElse(Map.of());

            String message = String.format("""
                            请确认您的订票信息：
                            📍 出发城市：%s
                            📍 到达城市：%s
                            📅 出发日期：%s
                            
                            确认无误请回复"确认"，如需修改请直接说明。
                            """,
                    slots.get("departure_city"),
                    slots.get("arrival_city"),
                    slots.get("departure_date")
            );

            return CompletableFuture.completedFuture(Map.of("confirmation_message", message));
        }
    }

    // ========== 订票节点 ==========

    static class BookingNode implements AsyncNodeActionWithConfig {

        @Override
        public CompletableFuture<Map<String, Object>> apply(OverAllState state, RunnableConfig config) {
            Map<String, Object> slots = (Map<String, Object>) state.value("slots").orElse(Map.of());

            // 模拟订票
            String orderId = "ORDER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String result = String.format("""
                            ✅ 订票成功！
                            
                            订单号：%s
                            出发：%s
                            到达：%s
                            日期：%s
                            
                            祝您旅途愉快！
                            """,
                    orderId,
                    slots.get("departure_city"),
                    slots.get("arrival_city"),
                    slots.get("departure_date")
            );

            return CompletableFuture.completedFuture(Map.of("booking_result", result));
        }
    }

    // ========== 构建 Graph ==========

    public static CompiledGraph createBookingGraph(ChatModel chatModel) throws Exception {
        // 定义节点
        SlotExtractionNode slotExtraction = new SlotExtractionNode(chatModel);
        ConfirmationNode confirmation = new ConfirmationNode();
        BookingNode booking = new BookingNode();

        // 定义状态策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("user_input", new ReplaceStrategy());
            strategies.put("slots", new ReplaceStrategy());
            strategies.put("human_feedback", new ReplaceStrategy());
            strategies.put("confirmation_message", new ReplaceStrategy());
            strategies.put("booking_result", new ReplaceStrategy());
            return strategies;
        };

        // 构建 Graph
        StateGraph graph = new StateGraph(keyStrategyFactory)
                .addNode("extract_slots", slotExtraction)
                .addNode("confirm", confirmation)
                .addNode("book", booking)
                .addEdge(StateGraph.START, "extract_slots")
                .addEdge("extract_slots", "confirm")
                .addEdge("confirm", "book")
                .addEdge("book", StateGraph.END);

        // 编译
        CompileConfig config = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(new MemorySaver())
                        .build())
                .build();

        return graph.compile(config);
    }

    // ========== 主流程 ==========

    public static void main(String[] args) throws Exception {
        ChatModel chatModel = createChatModel();  // 创建你的 ChatModel
        CompiledGraph graph = createBookingGraph(chatModel);

        Scanner scanner = new Scanner(System.in);
        String threadId = "user_" + System.currentTimeMillis();

        System.out.println("🤖 智能订票助手");
        System.out.println("请告诉我您的出行需求\n");

        // 第一次输入
        System.out.print("您：");
        String userInput = scanner.nextLine();

        handleConversation(graph, threadId, userInput, scanner);
    }

    private static ChatModel createChatModel() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder().apiKey("sk-****").build())
                .build();
    }

    private static void handleConversation(CompiledGraph graph, String threadId,
                                           String userInput, Scanner scanner) throws Exception {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        Map<String, Object> input = Map.of("user_input", userInput);
        Optional<NodeOutput> result = graph.invokeAndGetOutput(input, config);

        while (result.isPresent() && result.get() instanceof InterruptionMetadata) {
            InterruptionMetadata interruption = (InterruptionMetadata) result.get();

            // 显示系统提示
            String prompt = (String) interruption.metadata("prompt").orElse("请提供更多信息");
            System.out.println("\n🤖 " + prompt);

            // 等待用户输入
            System.out.print("\n您：");
            String userResponse = scanner.nextLine();

            // 更新状态
            RunnableConfig updateConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();

            Map<String, Object> updates = Map.of(
                    "user_input", userResponse,
                    "human_feedback", "provided"
            );

            RunnableConfig updatedConfig = graph.updateState(updateConfig, updates, interruption.node());

            // 恢复执行
            RunnableConfig resumeConfig = RunnableConfig.builder(updatedConfig)
                    .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, "placeholder")
                    .build();

            result = graph.invokeAndGetOutput((Map<String, Object>) null, resumeConfig);
        }

        // 完成
        if (result.isPresent()) {
            String bookingResult = (String) result.get().state().value("booking_result").orElse("");
            System.out.println("\n" + bookingResult);
        }
    }
}