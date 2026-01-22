package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ListFilesTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ReadFileTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.WriteFileTool;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.example.wx.interceptor.LogToolInterceptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangx
 * @description
 * @create 2026/1/22 20:05
 */
@Controller
public class ReactAgentController {
    private final ReactAgent reactAgent;

    private final Map<String, InterruptionMetadata> map = new ConcurrentHashMap<>();

    public ReactAgentController(ChatModel chatModel) {
        this.reactAgent = ReactAgent.builder()
                .name("agent")
                .description("This is a react agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(
                        ReadFileTool.createReadFileToolCallback(ReadFileTool.DESCRIPTION),
                        WriteFileTool.createWriteFileToolCallback(WriteFileTool.DESCRIPTION),
                        ListFilesTool.createListFilesToolCallback(ListFilesTool.DESCRIPTION)
                )
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("write_file", "Write File should be approved")
                        .build())
                .interceptors(new LogToolInterceptor())
                .build();
    }

    @GetMapping("/invoke")
    @ResponseBody
    public List<InterruptionMetadata.ToolFeedback> invoke(
            @RequestParam(value = "query", defaultValue = "你是谁") String query,
            @RequestParam(value = "threadId", defaultValue = "123123123123") String threadId
    ) throws Exception {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        NodeOutput nodeOutput = reactAgent.invokeAndGetOutput(query, runnableConfig).orElseThrow();
        if (nodeOutput instanceof InterruptionMetadata metadata) {
            map.put(threadId, metadata);
            return metadata.toolFeedbacks();
        }
        return List.of();
    }

    @PostMapping("/feedback")
    @ResponseBody
    public String feedback(
             @RequestBody List<Feedback> feedbacks,
            @RequestParam("threadId") String threadId
    ) throws Exception {
        InterruptionMetadata metadata = map.get(threadId);
        if (metadata == null) {
            return "no metadata found";
        }
        if (metadata.toolFeedbacks().size() != feedbacks.size()) {
            return "feedback size not match";
        }

        InterruptionMetadata.Builder newBuilder = InterruptionMetadata.builder()
                .nodeId(metadata.node())
                .state(metadata.state());
        for (int i = 0; i < feedbacks.size(); i++) {
            var toolFeedback = metadata.toolFeedbacks().get(i);
            InterruptionMetadata.ToolFeedback.Builder editedFeedbackBuilder = InterruptionMetadata.ToolFeedback
                    .builder(toolFeedback);
            if (feedbacks.get(i).isApproved()) {
                editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED);
            } else {
                editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                        .description(feedbacks.get(i).feedback());
            }
            newBuilder.addToolFeedback(editedFeedbackBuilder.build());
        }
        RunnableConfig resumeRunnableConfig = RunnableConfig.builder().threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, newBuilder.build())
                .build();
        reactAgent.invokeAndGetOutput("", resumeRunnableConfig);
        return "success";
    }

    @GetMapping
    public String index() {
        return "index";
    }

    public record Feedback(boolean isApproved, String feedback) {
    }


}
