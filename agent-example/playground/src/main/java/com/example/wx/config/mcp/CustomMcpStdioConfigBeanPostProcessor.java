package com.example.wx.config.mcp;

import com.example.wx.entiy.mcp.McpServerConfig;
import com.example.wx.exceptions.AppException;
import com.example.wx.utils.McpServerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.client.autoconfigure.StdioTransportAutoConfiguration;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Map;

import static com.example.wx.utils.McpServerUtils.getMcpLibsAbsPath;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 18:00
 */
public class CustomMcpStdioConfigBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CustomMcpStdioConfigBeanPostProcessor.class);

    private final ObjectMapper objectMapper;

    private final McpStdioClientProperties mcpStdioClientProperties;

    public CustomMcpStdioConfigBeanPostProcessor(
            ObjectMapper objectMapper,
            McpStdioClientProperties mcpStdioClientProperties
    ) {
        this.objectMapper = objectMapper;
        this.mcpStdioClientProperties = mcpStdioClientProperties;
    }

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if (bean instanceof StdioTransportAutoConfiguration) {
            logger.debug("Enhancement McpStdioTransportConfiguration bean start: {}", beanName);

            McpServerConfig mcpServerConfig;
            try {
                mcpServerConfig = McpServerUtils.getMcpServerConfig();
                // Handle the jar relative path issue in the configuration file.
                for (Map.Entry<String, McpStdioClientProperties.Parameters> entry : mcpServerConfig.getMcpServers()
                        .entrySet()) {

                    if (entry.getValue() != null && entry.getValue().command().startsWith("java")) {

                        McpStdioClientProperties.Parameters serverConfig = entry.getValue();
                        String oldMcpLibsPath = McpServerUtils.getLibsPath(serverConfig.args());
                        String rewriteMcpLibsAbsPath = getMcpLibsAbsPath(McpServerUtils.getLibsPath(serverConfig.args()));
                        if (rewriteMcpLibsAbsPath != null) {
                            serverConfig.args().remove(oldMcpLibsPath);
                            serverConfig.args().add(rewriteMcpLibsAbsPath);
                        }
                    }
                }
                String msc = objectMapper.writeValueAsString(mcpServerConfig);
                logger.debug("Registry McpServer config: {}", msc);

                // write mcp client
                mcpStdioClientProperties.setServersConfiguration(new ByteArrayResource(msc.getBytes()));
                ((StdioTransportAutoConfiguration) bean).stdioTransports(this.mcpStdioClientProperties);
            } catch (IOException e) {
                throw new AppException(e.getMessage());
            }
            logger.debug("Enhancement McpStdioTransportConfiguration bean end: {}", beanName);
        }
        return bean;
    }
}
