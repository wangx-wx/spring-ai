package com.example.wx.interceptor;

import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:33
 */
@Data
@Builder
public class SkillMetadata {
    private String name;

    private String description;

    private String skillPath;

    private String source;

    @Builder.Default
    private String fullContent = null;

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
}
