package com.it_support_ticket_system.demo.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient aiWebClient(WebClient.Builder webClientBuilder, AppProperties appProperties) {
        int timeoutSeconds = appProperties.getAi().getTimeoutSeconds();
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
            .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        return webClientBuilder
            .baseUrl(appProperties.getAi().getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
