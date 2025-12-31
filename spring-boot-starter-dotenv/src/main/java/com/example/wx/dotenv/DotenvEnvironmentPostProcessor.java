package com.example.wx.dotenv;

import com.example.wx.dotenv.parser.DefaultDotenvParser;
import com.example.wx.dotenv.parser.DotenvParser;
import com.example.wx.dotenv.resolver.DefaultVariableResolver;
import com.example.wx.dotenv.resolver.VariableResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Spring Boot EnvironmentPostProcessor 实现
 * <p>
 * 在 Spring 容器初始化之前加载 .env 文件中的变量到 Environment 中，
 * 使得 @Value 注解和 application.yml 中的 ${VAR} 占位符能够正确解析。
 * <p>
 * 特性：
 * <ul>
 *   <li>自动查找项目根目录下的 .env 文件</li>
 *   <li>支持 ${VAR} 和 ${VAR:-default} 语法</li>
 *   <li>优先级最高，会覆盖 application.yml 和系统环境变量中的同名属性</li>
 *   <li>文件不存在时静默跳过</li>
 *   <li>解析失败时打印 ERROR 日志但不阻断启动</li>
 * </ul>
 *
 * @author wangx
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);

    private static final String DOTENV_FILE_NAME = ".env";

    /**
     * 设置为最高优先级，确保在其他 PostProcessor 之前执行
     */
    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    private final DotenvParser parser;
    private final VariableResolver resolver;

    public DotenvEnvironmentPostProcessor() {
        this(new DefaultDotenvParser(), new DefaultVariableResolver());
    }

    public DotenvEnvironmentPostProcessor(DotenvParser parser, VariableResolver resolver) {
        this.parser = parser;
        this.resolver = resolver;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dotenvPath = findDotenvFile();

        if (dotenvPath == null) {
            log.debug(".env 文件不存在，跳过加载");
            return;
        }

        try {
            // 1. 解析 .env 文件
            Map<String, String> rawVariables = parser.parse(dotenvPath);
            if (rawVariables.isEmpty()) {
                log.debug(".env 文件为空，跳过加载");
                return;
            }

            // 2. 解析变量引用
            Map<String, String> resolvedVariables = resolver.resolveAll(rawVariables);

            // 3. 添加到 Environment（最高优先级）
            DotenvPropertySource propertySource = new DotenvPropertySource(resolvedVariables);
            environment.getPropertySources().addFirst(propertySource);

            log.info("成功从 .env 文件加载 {} 个变量", resolvedVariables.size());
            if (log.isDebugEnabled()) {
                resolvedVariables.keySet().forEach(key -> log.debug("  - {}", key));
            }

        } catch (IOException e) {
            log.error("读取 .env 文件失败: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("解析 .env 文件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 查找 .env 文件
     * 从当前工作目录查找
     */
    private Path findDotenvFile() {
        // 从当前工作目录查找
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path dotenvPath = workingDir.resolve(DOTENV_FILE_NAME);

        if (Files.exists(dotenvPath) && Files.isRegularFile(dotenvPath)) {
            log.debug("找到 .env 文件: {}", dotenvPath.toAbsolutePath());
            return dotenvPath;
        }

        return null;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

}
