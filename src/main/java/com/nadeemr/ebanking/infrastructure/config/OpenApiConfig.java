package com.nadeemr.ebanking.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${app.openapi.dev-url:http://localhost:8080}")
    private String devUrl;
    
    @Value("${app.openapi.prod-url:https://api.ebanking.nadeemr.com}")
    private String prodUrl;
    
    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");
        
        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");
        
        Contact contact = new Contact();
        contact.setEmail("api-support@nadeemr.com");
        contact.setName("NadeemR API Support");
        contact.setUrl("https://www.nadeemr.com");
        
        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("eBanking Transactions API")
                .version("1.0")
                .contact(contact)
                .description("REST API for paginated account transactions with Kafka integration")
                .license(license);
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authentication")));
    }
}
