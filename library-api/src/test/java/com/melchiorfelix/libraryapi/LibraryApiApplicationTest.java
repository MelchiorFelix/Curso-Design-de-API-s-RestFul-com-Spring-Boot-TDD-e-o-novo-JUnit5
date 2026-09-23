package com.melchiorfelix.libraryapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApiApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void createsAndReadsBooksThroughTheUpgradedJpaAndJsonStack() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Migration test","author":"Author","isbn":"migration-001"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("isbn").value("migration-001"));

        mvc.perform(get("/api/books").param("isbn", "migration-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].title").value("Migration test"));
    }

    @Test
    void jakartaValidationStillRejectsEmptyBooks() throws Exception {
        mvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.length()").value(3));
    }
}
