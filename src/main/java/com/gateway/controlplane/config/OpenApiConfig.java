package com.gateway.controlplane.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI controlPlaneOpenApi(
            @Value("${server.port:8081}") String serverPort,
            @Value("${spring.application.name:control-plane-service}") String applicationName
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title("Gateway Control Plane API")
                        .description("Administrative API for gateway overview, routes, policy rules, API keys, users, and nodes.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Gateway Platform Team"))
                        .license(new License()
                                .name("Internal Use")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description(applicationName + " local server")
                ));
    }
}
