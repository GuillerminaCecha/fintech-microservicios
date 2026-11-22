package com.fintech.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("customer-service")
                .description("Microservicio principal de clientes - TP final Diplomatura FinTech")
                .version("1.0.0"));
    }
}
