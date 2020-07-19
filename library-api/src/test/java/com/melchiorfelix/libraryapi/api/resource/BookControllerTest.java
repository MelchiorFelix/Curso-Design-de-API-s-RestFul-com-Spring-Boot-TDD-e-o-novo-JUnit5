package com.melchiorfelix.libraryapi.api.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melchiorfelix.libraryapi.api.dto.BookDTO;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.servvice.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    private MockMvc mvc;

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception{

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(101L).title("Sociedade da Caveira de Cristal").author("Andréa del Fuego").isbn("001").build();
        given(service.save(any(Book.class)))
                .willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect( jsonPath("id").isNotEmpty())
                .andExpect( jsonPath("title").value(dto.getTitle()))
                .andExpect( jsonPath("author").value(dto.getAuthor()))
                .andExpect( jsonPath("isbn").value(dto.getIsbn()))
        ;
    }



    @Test
    @DisplayName("Deve lançacar erro de validação quando não houver dados sufientes para criação do livro.")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um licro com isbn já utilizado por outro.")
    public void createBookWithDuplicatedIsbn() throws  Exception{
        //cenario
        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String mensagemErro = "Isbn já cadastrado";
        given(service.save(any(Book.class))).willThrow(new BusinessException(mensagemErro));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }

     @Test
     @DisplayName("Deve obter informacoes de um livro.")
     public void getBookDetails() throws Exception {
        //cenario
         Long id = 1L;
         Book book = Book.builder().id(id).title("As aventuras").author("João").isbn("123").build();
         given(service.getById(id)).willReturn(Optional.of(book));

         //execucao
         MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                 .get(BOOK_API.concat("/" + id))
                 .accept(MediaType.APPLICATION_JSON);

         //verificao
         mvc.perform(request)
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("id").value(1L))
                 .andExpect(jsonPath("title").value("As aventuras"))
                 .andExpect(jsonPath("author").value("João"))
                 .andExpect(jsonPath("isbn").value("123"));

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void  bookNotFound() throws Exception{
        //cenario
        Long id = 1L;
        given(service.getById(anyLong())).willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //verificao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }


    private BookDTO createNewBook() {
        return BookDTO.builder().title("Sociedade da Caveira de Cristal").author("Andréa del Fuego").isbn("001").build();
    }











}
