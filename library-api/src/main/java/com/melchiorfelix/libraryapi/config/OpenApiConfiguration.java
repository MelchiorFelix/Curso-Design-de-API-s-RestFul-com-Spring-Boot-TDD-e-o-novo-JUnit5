package com.melchiorfelix.libraryapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI libraryOpenApi(@Value("${library.api.version}") String version) {
        return new OpenAPI()
                .info(new Info().title("Library API").version(version).description(
                        "Manage library books, physical copies, members, and circulation. "
                        + "Register a member and an available copy before checkout. "
                        + "Loan periods and borrowing limits are configurable."))
                .components(new Components()
                        .addSchemas("ApiErrors", new ObjectSchema()
                                .addProperty("errors", new ArraySchema().items(new StringSchema()))
                                .required(List.of("errors")))
                        .addResponses("BadRequest", errorResponse(
                                "Invalid request or circulation rule violation", "Member has overdue loans"))
                        .addResponses("NotFound", errorResponse(
                                "Requested record was not found", "Member not found"))
                        .addResponses("Conflict", errorResponse(
                                "Duplicate identifier, referenced record, or concurrent operation",
                                "Another circulation operation is in progress; please retry")));
    }

    private ApiResponse errorResponse(String description, String example) {
        return new ApiResponse().description(description).content(new Content()
                .addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiErrors"))
                        .example(Map.of("errors", List.of(example)))));
    }
}
