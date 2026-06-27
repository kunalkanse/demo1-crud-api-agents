package com.ecommerce.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for API documentation.
 * Generates interactive API documentation accessible at /swagger-ui.html
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Bearer Token for API authentication")))
                .info(new Info()
                        .title("Product CRUD API")
                        .version("1.0.0")
                        .description("E-Commerce Product Management API\n\n" +
                                "A secure REST API for managing products in an e-commerce catalog.\n\n" +
                                "## Authentication\n" +
                                "All endpoints require a Bearer JWT token in the Authorization header:\n" +
                                "```\n" +
                                "Authorization: Bearer <your-jwt-token>\n" +
                                "```\n\n" +
                                "## Authorization\n" +
                                "- **PRODUCT_VIEWER**: Read-only access to products\n" +
                                "- **PRODUCT_MANAGER**: Full CRUD access (create, read, update, delete)")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@ecommerce-store.com")));
    }
}
