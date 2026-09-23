package com.curso.melchior.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void servesClienteWithTheUpgradedWebStack() throws Exception {
        mvc.perform(get("/api/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("nome").value("John"))
                .andExpect(jsonPath("cpf").value("000.000.000-00"));
    }
}
