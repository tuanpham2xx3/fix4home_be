package com.fix4home.fix4home.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fix4Home API Documentation")
                        .description("RESTful API documentation for Fix4Home home services platform")
                        .version("1.0.0"));
    }
}
