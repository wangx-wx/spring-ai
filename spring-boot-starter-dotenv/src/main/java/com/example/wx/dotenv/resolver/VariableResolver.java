package com.example.wx.dotenv.resolver;

import java.util.Map;

/**
 * 变量引用解析器接口
 * <p>
 * 负责解析值中的变量引用，如 ${VAR} 或 ${VAR:-default}
 *
 * @author wangx
 */
public interface VariableResolver {

    /**
     * 解析所有变量中的引用
     *
     * @param variables 原始变量映射（值可能包含 ${VAR} 引用）
     * @return 解析后的变量映射（所有引用已替换为实际值）
     */
    Map<String, String> resolveAll(Map<String, String> variables);

    /**
     * 解析单个值中的变量引用
     *
     * @param value             原始值
     * @param resolvedVariables 已解析的变量映射（用于查找引用）
     * @return 解析后的值
     */
    String resolve(String value, Map<String, String> resolvedVariables);

}
