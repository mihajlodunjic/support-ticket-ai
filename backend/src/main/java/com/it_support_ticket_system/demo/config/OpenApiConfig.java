package com.it_support_ticket_system.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helpdeskOpenApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI().info(
            new Info()
                .title("Helpdesk Backend API")
                .description("Spring Boot backend API for the intelligent helpdesk system.")
                .version(applicationName + "-v1")
        );
    }
}
