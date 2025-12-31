package com.example.wx.dotenv;

import org.springframework.core.env.EnumerablePropertySource;

import java.util.Map;

/**
 * .env 文件的 PropertySource 实现
 * <p>
 * 将 .env 文件中的变量作为 Spring Environment 的属性源
 *
 * @author wangx
 */
public class DotenvPropertySource extends EnumerablePropertySource<Map<String, String>> {

    public static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenv";

    public DotenvPropertySource(Map<String, String> source) {
        super(DOTENV_PROPERTY_SOURCE_NAME, source);
    }

    public DotenvPropertySource(String name, Map<String, String> source) {
        super(name, source);
    }

    @Override
    public String[] getPropertyNames() {
        return this.source.keySet().toArray(new String[0]);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.get(name);
    }

}
