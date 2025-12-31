package com.example.wx.dotenv.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认的变量引用解析器实现
 * <p>
 * 支持以下语法：
 * <ul>
 *   <li>${VAR} - 简单变量引用</li>
 *   <li>${VAR:-default} - 带默认值的变量引用</li>
 * </ul>
 * <p>
 * 解析顺序：
 * <ol>
 *   <li>先在 .env 文件已解析的变量中查找</li>
 *   <li>再从系统环境变量中查找</li>
 *   <li>如果都找不到且有默认值，使用默认值</li>
 *   <li>如果都找不到且无默认值，保留原始 ${VAR} 并打印警告</li>
 * </ol>
 *
 * @author wangx
 */
public class DefaultVariableResolver implements VariableResolver {

    private static final Logger log = LoggerFactory.getLogger(DefaultVariableResolver.class);

    /**
     * 匹配 ${VAR} 或 ${VAR:-default} 格式
     * 捕获组1: 变量名
     * 捕获组2: 默认值（可选，包含 :- 前缀）
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}:]+)(:-[^}]*)?}");

    @Override
    public Map<String, String> resolveAll(Map<String, String> variables) {
        Map<String, String> resolved = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String resolvedValue = resolve(value, resolved);
            resolved.put(key, resolvedValue);
        }

        return resolved;
    }

    @Override
    public String resolve(String value, Map<String, String> resolvedVariables) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(value);

        while (matcher.find()) {
            String varName = matcher.group(1);
            String defaultPart = matcher.group(2);
            String defaultValue = (defaultPart != null) ? defaultPart.substring(2) : null;

            String replacement = lookupVariable(varName, resolvedVariables, defaultValue);
            // 需要对替换值中的特殊字符进行转义
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        // 递归解析，处理嵌套引用
        String resolvedValue = result.toString();
        if (resolvedValue.contains("${") && !resolvedValue.equals(value)) {
            return resolve(resolvedValue, resolvedVariables);
        }

        return resolvedValue;
    }

    /**
     * 查找变量值
     * 优先级：.env 已解析变量 > 系统环境变量 > 默认值
     */
    private String lookupVariable(String varName, Map<String, String> resolvedVariables, String defaultValue) {
        // 1. 先从 .env 已解析的变量中查找
        if (resolvedVariables.containsKey(varName)) {
            return resolvedVariables.get(varName);
        }

        // 2. 再从系统环境变量中查找
        String envValue = System.getenv(varName);
        if (envValue != null) {
            return envValue;
        }

        // 3. 使用默认值
        if (defaultValue != null) {
            return defaultValue;
        }

        // 4. 都找不到，打印警告并保留原始格式
        log.warn("无法解析变量引用: ${}, 变量 '{}' 未定义且无默认值", varName, varName);
        return "${" + varName + "}";
    }

}
