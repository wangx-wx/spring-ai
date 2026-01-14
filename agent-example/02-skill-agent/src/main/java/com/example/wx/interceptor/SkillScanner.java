package com.example.wx.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:42
 */
@Slf4j
public class SkillScanner {

    private final Yaml yaml = new Yaml();

    public List<SkillMetadata> scan(String skillsDirectory) {
        return scan(skillsDirectory, "user");
    }

    public List<SkillMetadata> scan(String skillsDirectory, String source) {
        List<SkillMetadata> skills = new ArrayList<>();
        Path skillsPath = Path.of(skillsDirectory);

        if (!Files.exists(skillsPath)) {
            log.warn("Skills directory does not exist: {}", skillsDirectory);
            return skills;
        }

        if (!Files.isDirectory(skillsPath)) {
            log.warn("Skills path is not a directory: {}", skillsDirectory);
            return skills;
        }

        try (Stream<Path> paths = Files.list(skillsPath)) {
            paths.filter(Files::isDirectory)
                    .forEach(skillDir -> {
                        try {
                            SkillMetadata metadata = loadSkill(skillDir, source);
                            if (metadata != null) {
                                skills.add(metadata);
                                log.info("Loaded skill: {} from {}", metadata.getName(), skillDir);
                            }
                        } catch (Exception e) {
                            log.error("Failed to load skill from {}: {}", skillDir, e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to scan skills directory {}: {}", skillsDirectory, e.getMessage(), e);
        }

        log.info("Discovered {} skills from {}", skills.size(), skillsDirectory);
        return skills;
    }

    public SkillMetadata loadSkill(Path skillDir) {
        return loadSkill(skillDir, "user");
    }

    public SkillMetadata loadSkill(Path skillDir, String source) {
        Path skillFile = skillDir.resolve("SKILL.md");
        if (!Files.exists(skillFile)) {
            log.warn("SKILL.md not found at: {}", skillFile);
            return null;
        }

        try {
            String content = Files.readString(skillFile);
            Map<String, Object> frontmatter = parseFrontmatter(content);
            if (frontmatter == null || frontmatter.isEmpty()){
                log.warn("No frontmatter found in SKILL.md at: {}", skillFile);
                return null;
            }
            String name = (String)frontmatter.get("name");
            String description = (String)frontmatter.get("description");

            if (!StringUtils.hasText(name)) {
                log.warn("No name specified for skill at: {}", skillFile);
                return null;
            }
            if (!StringUtils.hasText(description)) {
                log.warn("No description specified for skill at: {}", skillFile);
                return null;
            }
            return SkillMetadata.builder()
                    .name(name)
                    .skillPath(skillDir.toString())
                    .source(source)
                    .description(description).build();
        } catch (IOException e) {
            log.error("Failed to read skill file {}: {}", skillFile, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Failed to load skill from {}: {}", skillDir, e.getMessage(), e);
            return null;
        }
    }

    private Map<String, Object> parseFrontmatter(String content) {
        if (!content.startsWith("---")) {
            return null;
        }

        int endIndex = content.indexOf("---", 3);
        if (endIndex == -1) {
            return null;
        }

        String frontmatterStr = content.substring(3, endIndex).trim();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> frontmatter = yaml.load(frontmatterStr);
            return frontmatter;
        } catch (Exception e) {
            log.error("Failed to parse YAML frontmatter: {}", e.getMessage(), e);
            return null;
        }
    }
}
