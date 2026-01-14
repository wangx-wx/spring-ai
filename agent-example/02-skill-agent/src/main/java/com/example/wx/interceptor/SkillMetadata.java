package com.example.wx.interceptor;

import lombok.Data;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:33
 */
@Data
public class SkillMetadata {
    private String name;

    private String description;

    private String skillPath;

    private String source;

    private String fullContent;

    public String loadFullContent() throws IOException {
        if (fullContent == null) {
            Path skillFile = Path.of(skillPath, "SKILL.md");
            if (!Files.exists(skillFile)) {
                throw new IOException("SKILL.md not found at: " + skillFile);
            }

            String rawContent = Files.readString(skillFile);
            fullContent = removeFrontmatter(rawContent);
        }
        return fullContent;
    }

    private String removeFrontmatter(String content) {
        if (!content.startsWith("---")) {
            return content;
        }

        int endIndex = content.indexOf("---", 3);
        if (endIndex == -1) {
            return content;
        }

        return content.substring(endIndex + 3).trim();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SkillMetadata metadata = new SkillMetadata();
        public Builder name(String name) {
            metadata.setName(name);
            return this;
        }

        public Builder description(String description) {
            metadata.description = description;
            return this;
        }

        public Builder skillPath(String skillPath) {
            metadata.skillPath = skillPath;
            return this;
        }

        public Builder source(String source) {
            metadata.source = source;
            return this;
        }
        public SkillMetadata build() {
            Assert.hasText(metadata.name, "Name must not be empty");
            Assert.hasText(metadata.description, "Description must not be empty");
            Assert.hasText(metadata.skillPath, "Skill path must not be empty");
            return metadata;
        }
    }
}
