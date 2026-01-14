package com.example.wx.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ListFilesTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.ReadFileTool;
import com.alibaba.cloud.ai.graph.agent.extension.tools.filesystem.WriteFileTool;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool;
import com.example.wx.interceptor.SkillsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:28
 */
@Service
public class SkillsAgent {
    private static final Logger logger = LoggerFactory.getLogger(SkillsAgent.class);

    @Value("${skill.agent.skills-dir:skills}")
    private String skillsDir;

    public ReactAgent buildAgent(ChatModel chatModel) {

        Path skillsPath = Path.of(skillsDir).toAbsolutePath();
        logger.info("Skills directory: {}", skillsPath);

        if (!Files.exists(skillsPath)) {
            logger.error("Skills directory not found at: {}", skillsPath);
            throw new IllegalStateException("Skills directory not found");
        }

        logger.info("Skills directory exists, listing contents:");
        try (Stream<Path> paths = Files.list(skillsPath)) {
            paths.forEach(p -> logger.info("  - {}", p.getFileName()));
        } catch (IOException e) {
            logger.error("Failed to list directory", e);
        }

        SkillsInterceptor interceptor = SkillsInterceptor.builder()
                .userSkillsDirectory(skillsPath.toString())
                .autoScan(true)
                .build();

        logger.info("Skills loaded: {}", interceptor.getSkillCount());

        List<ToolCallback> tools = new ArrayList<>();
        tools.add(ReadFileTool.createReadFileToolCallback(ReadFileTool.DESCRIPTION));
        tools.add(WriteFileTool.createWriteFileToolCallback(WriteFileTool.DESCRIPTION));
        tools.add(ListFilesTool.createListFilesToolCallback(ListFilesTool.DESCRIPTION));
        tools.add(ShellTool.builder(System.getProperty("user.dir"))
                .build());

        ShellToolAgentHook hook = ShellToolAgentHook.builder()
                .shellToolName("shell")
                .build();

        return ReactAgent.builder()
                .name("skill-agent")
                .model(chatModel)
                .hooks(hook)
                .interceptors(interceptor)
                .tools(tools)
                .enableLogging(true)
                .build();
    }
}
