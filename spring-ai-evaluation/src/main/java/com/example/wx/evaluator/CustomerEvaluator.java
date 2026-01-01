package com.example.wx.evaluator;

import com.alibaba.cloud.ai.evaluation.LaajEvaluator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;

/**
 * @author wang.x
 * @description
 * @create 2025/12/31 15:08
 */
public class CustomerEvaluator extends LaajEvaluator {

    private final static String DEFAULT_EVALUATION_PROMPT_TEXT = """
            你是一个极其严格的忠实度评估专家，只关注答案是否完全由检索到的文档语义支持。你的唯一任务是判断给定答案是否100%忠实于提供的上下文文档，不允许任何外部知识、不允许合理推断、不允许“大概率是对的”。
            
            评估规则（必须严格遵守，每一条都不能违反）：
            1. 如果答案中任意一句、任意一个关键事实、数字、专有名词、因果关系、时间顺序在上下文文档中找不到明确支持，就必须判定为不忠实。
            2. 答案不能“比文档说得更多”，哪怕多说一个字的额外信息也算不忠实。
            3. 答案可以比文档说得少（省略信息），但绝不能多说、不能改说、不能创造。
            4. 同义改写允许，但必须是100%语义等价，不能改变或增加任何语义。
            5. 如果答案把多个文档的信息拼在一起，只要拼的是原文语义且没有增加新信息，属于忠实。
            6. 如果文档中存在明显冲突，答案只采纳了一部分，属于忠实（因为它没有凭空造信息）。
            7. 答案说“根据检索到的文档，无法确定XX”属于完全忠实。
            8. 答案说“文档中未提及”属于完全忠实。
            
            评分标准（只输出0或1）：
            - 1 = 完全忠实（答案的所有信息都在上下文文档中有明确出处，没有任何额外信息或改动）
            - 0 = 不忠实（答案中存在任何一条信息在上下文文档中找不到明确支持，或增加了额外信息）
            
            严禁输出0.5、0.8等中间分数，严禁输出“基本忠实”“大致忠实”等模糊表述。
            
            输出格式（必须严格遵守）：
            \\{
              "faithfulness": 0或1,
              "explanation": "用最简洁的中文说明判定理由，指出具体哪句/哪个事实不忠实（如果得分0），或说明“答案所有信息均来自文档，无任何增加或改动”（如果得分1）。字数控制在60字以内。"
            \\}
            
            上下文文档：
            {retrieved_contexts}
            
            问题：
            {question}
            
            答案：
            {answer}
            
            现在开始评估
            """;

    public CustomerEvaluator(ChatClient.Builder chatClientBuilder) {
        super(chatClientBuilder);
    }

    public CustomerEvaluator(ChatClient.Builder chatClientBuilder, String evaluationPromptText) {
        super(chatClientBuilder, evaluationPromptText);
    }

    public CustomerEvaluator(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        super(chatClientBuilder, objectMapper);
    }

    public CustomerEvaluator(ChatClient.Builder chatClientBuilder, String evaluationPromptText,
            ObjectMapper objectMapper) {
        super(chatClientBuilder, evaluationPromptText, objectMapper);
    }

    @Override
    protected String getDefaultEvaluationPrompt() {
        return DEFAULT_EVALUATION_PROMPT_TEXT;
    }

    @Override
    public String getName() {
        return "CustomerEvaluator";
    }

    @Override
    public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
        if (evaluationRequest == null) {
            throw new IllegalArgumentException("EvaluationRequest must not be null");
        }
        var response = doGetResponse(evaluationRequest);
        var context = doGetSupportingData(evaluationRequest);

        String llmEvaluationResponse = getChatClientBuilder().build()
                .prompt()
                .user(userSpec -> userSpec.text(getEvaluationPromptText())
                        .param("question", evaluationRequest.getUserText())
                        .param("retrieved_contexts", context)
                        .param("answer", response))
                .call()
                .content();

        JsonNode evaluationResponse = null;
        try {
            evaluationResponse = getObjectMapper().readTree(llmEvaluationResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        float score = (float) evaluationResponse.get("faithfulness").asDouble();
        String feedback = evaluationResponse.get("explanation").asText();
        boolean passing = score > 0;
        return new EvaluationResponse(passing, score, feedback, Collections.emptyMap());
    }
}
