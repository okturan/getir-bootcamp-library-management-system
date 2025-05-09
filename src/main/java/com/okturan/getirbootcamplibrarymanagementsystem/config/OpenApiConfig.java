package com.okturan.getirbootcamplibrarymanagementsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                              .title("Library Management System API")
                              .description("API documentation for the Library Management System")
                              .version("1.0.0")
                              .contact(new Contact()
                                               .name("Library Management Team")
                                               .email("contact@library.com")
                                               .url("https://library.com"))
                              .license(new License()
                                               .name("Apache 2.0")
                                               .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Default Server URL")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                                    .addSecuritySchemes(securitySchemeName,
                                                        new SecurityScheme()
                                                                .name(securitySchemeName)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description(
                                                                        "Enter JWT Bearer token in the format: Bearer &lt;token&gt;"
                                                                )
                                    ));
    }

}
