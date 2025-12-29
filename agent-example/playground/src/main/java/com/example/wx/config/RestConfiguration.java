package com.example.wx.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:21
 */
@Configuration
public class RestConfiguration {
    @Bean
    public WebClient.Builder webClientBuilder() {

        return WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create().responseTimeout(Duration.of(600, ChronoUnit.SECONDS))
                        )
                );
    }
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(
                        ClientHttpRequestFactoryBuilder.reactor().withCustomizer(
                                factory -> {
                                    factory.setConnectTimeout(Duration.of(10, ChronoUnit.MINUTES));
                                    factory.setReadTimeout(Duration.of(10, ChronoUnit.MINUTES));
                                }
                        ).build()
                );
    }
}
