package com.example.wx.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangx
 * @description
 * @create 2025/7/28 21:27
 */
public enum ModelEnums {
    QWEN_PLUS("qwen-plus-2025-01-25", "dashscopeChatClient", "效果、速度、成本均衡"),
    DEEPSEEK_REASONER("deepseek-r1", "deepseekChatClient", "包含 671B 参数，激活 37B，在后训练阶段大规模使用了强化学习技术，在仅有极少标注数据的情况下，极大提升了模型推理能力，尤其在数学、代码、自然语言推理等任务上"),
    DEEPSEEK_CHAT("deepseek-chat", "deepseekChatClient", "MoE 模型，671B 参数，激活 37B，在 14.8T Token 上进行了预训练，在长文本、代码、数学、百科、中文能力上表现优秀");


    private final String name;

    private final String client;

    private final String description;


    ModelEnums(String name, String client, String description) {
        this.name = name;
        this.description = description;
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public String getClient() {
        return client;
    }

    public String getDescription() {
        return description;
    }

    public static List<String> getModels() {
        return Arrays.stream(ModelEnums.values()).map(ModelEnums::getName).toList();
    }

    public static ModelEnums getModelByType(String model) {
        for (ModelEnums modelEnum : ModelEnums.values()) {
            if (modelEnum.getName().equals(model)) {
                return modelEnum;
            }
        }
        return null;
    }
}
