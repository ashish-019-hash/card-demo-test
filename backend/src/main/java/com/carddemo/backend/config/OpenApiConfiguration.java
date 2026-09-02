package com.carddemo.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {
    @Bean
    OpenAPI cardDemoOpenApi() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("SESSION")).components(new Components()
                .addSecuritySchemes("SESSION", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE).name("JSESSIONID").description("Server-issued browser session cookie."))
                .addSchemas("ApiError", new ObjectSchema()
                        .addProperty("code", new StringSchema())
                        .addProperty("message", new StringSchema())
                        .addProperty("field", new StringSchema().nullable(true))
                        .addProperty("traceId", new StringSchema().nullable(true)))
                .addResponses("BadRequest", error("Invalid or missing request value."))
                .addResponses("Unauthorized", error("Authentication is required."))
                .addResponses("Forbidden", error("Administrator access is required."))
                .addResponses("NotFound", error("User ID was not found."))
                .addResponses("Conflict", error("Duplicate, no-change, or version conflict.")));
    }

    private ApiResponse error(String description) {
        return new ApiResponse().description(description).content(new io.swagger.v3.oas.models.media.Content()
                .addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType()
                        .schema(new io.swagger.v3.oas.models.media.Schema<>().$ref("#/components/schemas/ApiError"))));
    }
}
