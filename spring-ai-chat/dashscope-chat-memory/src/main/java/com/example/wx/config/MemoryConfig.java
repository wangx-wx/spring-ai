package com.example.wx.config;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.alibaba.cloud.ai.memory.jdbc.PostgresChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author wangx
 * @description
 * @create 2025/7/15 21:33
 */
@Configuration
public class MemoryConfig {
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.jdbc-url}")
    private String mysqlJdbcUrl;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.username}")
    private String mysqlUsername;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.password}")
    private String mysqlPassword;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.driver-class-name}")
    private String mysqlDriverClassName;


    @Value("${spring.ai.chat.memory.repository.jdbc.postgres.jdbc-url}")
    private String pgsqlJdbcUrl;
    @Value("${spring.ai.chat.memory.repository.jdbc.postgres.username}")
    private String pgsqlUsername;
    @Value("${spring.ai.chat.memory.repository.jdbc.postgres.password}")
    private String pgsqlPassword;
    @Value("${spring.ai.chat.memory.repository.jdbc.postgres.driver-class-name}")
    private String pgsqlDriverClassName;

    @Bean
    public MysqlChatMemoryRepository mysqlChatMemoryRepository() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(mysqlDriverClassName);
        dataSource.setUrl(mysqlJdbcUrl);
        dataSource.setUsername(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }

    @Bean
    public PostgresChatMemoryRepository pgsqlChatMemoryRepository() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(pgsqlDriverClassName);
        dataSource.setUrl(pgsqlJdbcUrl);
        dataSource.setUsername(pgsqlUsername);
        dataSource.setPassword(pgsqlPassword);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return PostgresChatMemoryRepository.postgresBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }
}
