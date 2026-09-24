package com.melchiorfelix.libraryapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void publishesLibraryMetadataAndOnlyLibraryEndpoints() throws Exception {
        JsonNode spec = specification();
        assertThat(spec.path("openapi").asText()).startsWith("3.");
        assertThat(spec.at("/info/title").asText()).isEqualTo("Library API");
        assertThat(spec.at("/info/version").asText()).isEqualTo("0.0.1-SNAPSHOT");
        List<String> paths = new ArrayList<>();
        spec.path("paths").properties().forEach(entry -> paths.add(entry.getKey()));
        assertThat(paths).hasSize(12).allMatch(path -> path.startsWith("/api/"));
        assertThat(paths).contains("/api/books", "/api/members", "/api/loans/overdue",
                "/api/books/{bookId}/copies", "/api/copies/{id}/withdraw");
        assertThat(spec.at("/paths/~1api~1loans/post/operationId").asText()).isEqualTo("checkout");
        assertThat(spec.at("/paths/~1api~1loans/post/tags/0").asText()).isEqualTo("Loans");
    }

    @Test
    void documentsRequiredCheckoutFieldsAndOptionalCopySelection() throws Exception {
        JsonNode spec = specification();
        JsonNode checkout = spec.at("/components/schemas/CheckoutRequest");
        List<String> required = new ArrayList<>();
        checkout.path("required").forEach(field -> required.add(field.asText()));
        assertThat(required).containsExactlyInAnyOrder("isbn", "memberId");
        assertThat(checkout.at("/properties/copyId/description").asText()).contains("omit");
        assertThat(spec.at("/components/schemas/MemberRequest/properties/email/format").asText()).isEqualTo("email");
        assertThat(spec.at("/components/schemas/BookDTO/properties/id/readOnly").asBoolean()).isTrue();
        assertThat(spec.at("/components/schemas/LoanDTO/properties/dueDate/format").asText()).isEqualTo("date");
        assertThat(spec.at("/paths/~1api~1loans/post/responses/201/content/application~1json/schema/type").asText())
                .isEqualTo("integer");
        assertThat(spec.at("/paths/~1api~1loans/post/responses/400/$ref").asText())
                .isEqualTo("#/components/responses/BadRequest");
        assertThat(spec.at("/components/responses/BadRequest/content/application~1json/schema/$ref").asText())
                .isEqualTo("#/components/schemas/ApiErrors");
    }

    @Test
    void exposesOptionalFiltersAndPaginationAsQueryParameters() throws Exception {
        JsonNode spec = specification();
        JsonNode bookSearch = spec.at("/paths/~1api~1books/get");
        assertThat(bookSearch.has("requestBody")).isFalse();
        Set<String> expected = Set.of("id", "title", "author", "isbn", "page", "size", "sort");
        List<String> names = new ArrayList<>();
        for (JsonNode parameter : bookSearch.path("parameters")) {
            names.add(parameter.path("name").asText());
            assertThat(parameter.path("in").asText()).isEqualTo("query");
            assertThat(parameter.path("required").asBoolean()).isFalse();
        }
        assertThat(names).containsExactlyInAnyOrderElementsOf(expected);
        List<String> loanFilters = new ArrayList<>();
        spec.at("/paths/~1api~1loans/get/parameters").forEach(p -> loanFilters.add(p.path("name").asText()));
        assertThat(loanFilters).contains("memberId", "returned", "overdue", "page", "size", "sort");
    }

    @Test
    void servesSwaggerUiAndPointsItToTheLocalSpecification() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("swagger-ui-bundle.js")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("url").value("/v3/api-docs"));
    }

    @Test
    void publishesYamlForExternalApiTools() throws Exception {
        mvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi:")))
                .andExpect(content().string(containsString("title: Library API")));
    }

    private JsonNode specification() throws Exception {
        String body = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }
}
