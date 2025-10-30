package com.tq.exchangehub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "TQ Exchange Hub API",
                version = "v1",
                description = "REST endpoints that power the TQ Exchange Hub platform."),
        security = {@SecurityRequirement(name = "bearerAuth")})
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Provide the JWT access token returned by the authentication endpoints.")
public class OpenApiConfig {

    @Bean
    public OpenAPI exchangeHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TQ Exchange Hub API")
                        .version("v1")
                        .description("REST endpoints that power the TQ Exchange Hub platform."))
                .addServersItem(new Server().url("http://localhost:8080").description("Local environment"));
    }
}
