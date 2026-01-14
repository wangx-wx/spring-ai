package com.example.wx.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:32
 */
@Slf4j
public class SkillsInterceptor extends ModelInterceptor {
    private final SkillRegistry skillRegistry;
    private final String userSkillsDirectory;
    private final String projectSkillsDirectory;
    private volatile boolean skillsLoaded = false;

    private SkillsInterceptor(Builder builder) {
        this.skillRegistry = new SkillRegistry();
        this.userSkillsDirectory = builder.userSkillsDirectory;
        this.projectSkillsDirectory = builder.projectSkillsDirectory;

        if (builder.autoScan) {
            loadSkills();
        }
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        if (!skillsLoaded) {
            synchronized (this) {
                if (!skillsLoaded) {
                    loadSkills();
                }
            }
        }

        List<SkillMetadata> skills = skillRegistry.listAll();
        if (skills.isEmpty()) {
            return handler.call(request);
        }

        String skillsPrompt = buildSkillsPrompt(skills);
        SystemMessage enhanced = enhanceSystemMessage(request.getSystemMessage(), skillsPrompt);
        log.info("Enhanced system message:\n{}", enhanced.getText());
        ModelRequest modified = ModelRequest.builder(request)
                .systemMessage(enhanced)
                .build();
        return handler.call(modified);
    }

    @Override
    public String getName() {
        return "skills";
    }

    private void loadSkills() {
        SkillScanner skillScanner = new SkillScanner();
        Map<String, SkillMetadata> mergeSkills = new HashMap<>();

        if (StringUtils.hasText(userSkillsDirectory)) {
            Path path = Path.of(userSkillsDirectory);
            if (Files.exists(path)) {
                List<SkillMetadata> userSkills = skillScanner.scan(userSkillsDirectory, "user");
                userSkills.forEach(skill -> mergeSkills.put(skill.getName(), skill));
            }
            log.info("Loaded user skills from: {}", userSkillsDirectory);
        }

        if (StringUtils.hasText(projectSkillsDirectory)) {
            Path projectPath = Path.of(projectSkillsDirectory);
            if (Files.exists(projectPath)) {
                List<SkillMetadata> projectSkills = skillScanner.scan(projectSkillsDirectory, "proj");
                projectSkills.forEach(skill -> mergeSkills.put(skill.getName(), skill));
            }
            log.info("Loaded project skills from: {}", projectSkillsDirectory);
        }
        skillRegistry.registerAll(new ArrayList<>(mergeSkills.values()));
        skillsLoaded = true;
        log.info("Total skills loaded: {}", skillRegistry.size());
    }

    private String buildSkillsPrompt(List<SkillMetadata> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                ## Skills System
                You have access to a skills library that provides specialized knowledge and workflows.
                
                **CRITICAL: Skills are NOT tools!**
                Skills are instruction documents that guide you on how to use your available tools.
                You cannot directly call a skill - you must first read its SKILL.md file to understand the workflow.
                """);

        List<SkillMetadata> userSkills = new ArrayList<>();
        List<SkillMetadata> projectSkills = new ArrayList<>();

        for (SkillMetadata skill : skills) {
            if ("project".equals(skill.getSource())) {
                projectSkills.add(skill);
            } else {
                userSkills.add(skill);
            }
        }

        if (!userSkills.isEmpty() || !projectSkills.isEmpty()) {
            sb.append("**Skills Locations:**\n");
            if (!userSkills.isEmpty()) {
                sb.append("- User Skills: Global skills available across all projects\n");
            }
            if (!projectSkills.isEmpty()) {
                sb.append("- Project Skills: Project-specific skills (override user skills with same name)\n");
            }
            sb.append("\n");
        }
        sb.append("**Available Skills:**\n\n");

        if (!userSkills.isEmpty()) {
            sb.append("*User Skills:*\n");
            for (SkillMetadata skill : userSkills) {
                sb.append(String.format("- **%s** (skill guide): %s\n", skill.getName(), skill.getDescription()));
                sb.append(String.format("  → MUST read `%s/SKILL.md` first to learn how to use this skill\n", skill.getSkillPath()));
            }
            sb.append("\n");
        }

        if (!projectSkills.isEmpty()) {
            sb.append("*Project Skills:*\n");
            for (SkillMetadata skill : projectSkills) {
                sb.append(String.format("- **%s** (skill guide): %s\n", skill.getName(), skill.getDescription()));
                sb.append(String.format("  → MUST read `%s/SKILL.md` first to learn how to use this skill\n", skill.getSkillPath()));
            }
            sb.append("\n");
        }

        sb.append("""
                *How to Use Skills (MANDATORY Process):**
                
                When a user's request matches a skill's description, you MUST follow this process:
                1. **Read the SKILL.md file**: Use read_file tool with the path shown above
                2. **Understand the workflow**: The SKILL.md contains step-by-step instructions
                3. **Use your available tools**: Follow the skill's instructions to use tools like shell, read_file, write_file, etc.
                4. **Access supporting files**: If the skill references other files, read them using read_file with absolute paths
                
                **Example Workflow:**
                User asks: "Search for papers about transformers"
                → You recognize arxiv-search skill applies
                → You call: read_file("path/to/arxiv-search/SKILL.md")
                → You learn the skill requires executing a Python script with shell tool
                → You call: shell(command="python3 path/to/arxiv_search.py 'transformers'")
                
                **Important Notes:**
                - Never try to call a skill directly as a tool (e.g., arxiv-search() is WRONG)
                - Always read SKILL.md first - it contains the actual instructions
                - Skills guide you to use your existing tools in specific ways
                - Do not mention the skill name to users unless asked - seamlessly apply its logic
                """);
        return sb.toString();
    }

    private SystemMessage enhanceSystemMessage(SystemMessage systemMessage, String skillsPrompt) {
        if (systemMessage == null) {
            return new SystemMessage(skillsPrompt);
        }
        return new SystemMessage(systemMessage.getText() + "\n\n" + skillsPrompt);
    }

    public synchronized void reloadSkills() {
        log.info("Reloading skills...");
        skillRegistry.clear();
        skillsLoaded = false;
        loadSkills();
    }

    public synchronized void loadSkill(String skillDirectory) {
        if (skillDirectory == null || skillDirectory.isEmpty()) {
            throw new IllegalArgumentException("Skill directory cannot be null or empty");
        }

        try {
            SkillScanner scanner = new SkillScanner();
            SkillMetadata skill = scanner.loadSkill(Path.of(skillDirectory));

            if (skill == null) {
                throw new IllegalStateException("Failed to load skill from " + skillDirectory);
            }

            skillRegistry.register(skill);
            log.info("Loaded skill '{}' from {}", skill.getName(), skillDirectory);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Re-throw validation and state exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error loading skill from {}: {}", skillDirectory, e.getMessage(), e);
            throw new RuntimeException("Failed to load skill from " + skillDirectory, e);
        }
    }

    /**
     * Unloads a skill by name.
     *
     * @param skillName The name of the skill to unload (must not be null or empty)
     * @throws IllegalArgumentException if skillName is null or empty
     * @throws IllegalStateException if skill does not exist
     */
    public synchronized void unloadSkill(String skillName) {
        if (skillName == null || skillName.isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be null or empty");
        }

        if (!skillRegistry.contains(skillName)) {
            throw new IllegalStateException("Skill not found: " + skillName +
                    ". Use hasSkill() to check if skill exists before unloading.");
        }

        skillRegistry.unregister(skillName);
        log.info("Unloaded skill '{}'", skillName);
    }

    public int getSkillCount() {
        return skillRegistry.size();
    }

    public boolean hasSkill(String skillName) {
        return skillRegistry.contains(skillName);
    }

    public List<SkillMetadata> listSkills() {
        return skillRegistry.listAll();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userSkillsDirectory;
        private String projectSkillsDirectory;
        private boolean autoScan = true;

        public Builder userSkillsDirectory(String directory) {
            this.userSkillsDirectory = directory;
            return this;
        }

        public Builder projectSkillsDirectory(String directory) {
            this.projectSkillsDirectory = directory;
            return this;
        }

        public Builder autoScan(boolean autoScan) {
            this.autoScan = autoScan;
            return this;
        }

        public SkillsInterceptor build() {
            return new SkillsInterceptor(this);
        }
    }
}
