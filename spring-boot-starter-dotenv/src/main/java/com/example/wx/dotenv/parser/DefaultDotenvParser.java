package com.example.wx.dotenv.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认的 .env 文件解析器实现
 * <p>
 * 支持以下语法：
 * <ul>
 *   <li>KEY=value - 基本键值对</li>
 *   <li># comment - 注释行</li>
 *   <li>KEY="value with spaces" - 双引号包裹的值</li>
 *   <li>KEY='value with spaces' - 单引号包裹的值</li>
 *   <li>KEY=${OTHER_KEY} - 变量引用（由 VariableResolver 处理）</li>
 * </ul>
 *
 * @author wangx
 */
public class DefaultDotenvParser implements DotenvParser {

    private static final Logger log = LoggerFactory.getLogger(DefaultDotenvParser.class);

    @Override
    public Map<String, String> parse(Path path) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(path);

        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            String line = lines.get(lineNum).trim();

            // 跳过空行和注释行
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // 查找第一个等号
            int equalsIndex = line.indexOf('=');
            if (equalsIndex == -1) {
                log.warn(".env 文件第 {} 行格式无效，缺少等号: {}", lineNum + 1, line);
                continue;
            }

            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1);

            // 验证 key 是否有效
            if (key.isEmpty()) {
                log.warn(".env 文件第 {} 行格式无效，键名为空: {}", lineNum + 1, line);
                continue;
            }

            // 处理值：去除引号
            value = stripQuotes(value.trim());

            result.put(key, value);
        }

        return result;
    }

    /**
     * 去除值两端的引号（双引号或单引号）
     * 遵循 YAML 的处理逻辑
     */
    private String stripQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }

        char firstChar = value.charAt(0);
        char lastChar = value.charAt(value.length() - 1);

        // 双引号
        if (firstChar == '"' && lastChar == '"') {
            return value.substring(1, value.length() - 1);
        }

        // 单引号
        if (firstChar == '\'' && lastChar == '\'') {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

}
