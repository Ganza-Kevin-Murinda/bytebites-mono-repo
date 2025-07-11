package com.restaurant_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI byteBitesOpenAPI() {
        io.swagger.v3.oas.models.servers.Server devServer = new io.swagger.v3.oas.models.servers.Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Local server");

        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Contact contact = new Contact();
        contact.setEmail("support@bytebites.com");
        contact.setName("ByteBites Team");
        contact.setUrl("https://api.bytebites.com");

        Info info = new Info()
                .title("ByteBites Restaurant Service API")
                .version("1.0")
                .contact(contact)
                .description("Restaurant and Menu Management Service for ByteBites Platform");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", jwtScheme)
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}