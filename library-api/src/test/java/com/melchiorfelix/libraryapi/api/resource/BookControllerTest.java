package com.melchiorfelix.libraryapi.api.resource;

import tools.jackson.databind.ObjectMapper;
import com.melchiorfelix.libraryapi.api.dto.BookDTO;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.service.BookService;
import com.melchiorfelix.libraryapi.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    BookService service;

    @MockitoBean
    LoanService loanService;

    @Test
    @DisplayName("Should create a book successfully")
    public void createBookTest() throws Exception{

        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(101L).title("The Crystal Skull Society").author("Andréa del Fuego").isbn("001").build();
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
    @DisplayName("Should reject a book with missing required fields")
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
    @DisplayName("Should reject creating a book with a duplicate ISBN")
    public void createBookWithDuplicatedIsbn() throws  Exception{
        // Arrange
        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);
        String errorMessage = "ISBN already registered";
        given(service.save(any(Book.class))).willThrow(new BusinessException(errorMessage));

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Assert
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(errorMessage));
    }

     @Test
     @DisplayName("Should retrieve book details")
     public void getBookDetails() throws Exception {
        // Arrange
         Long id = 1L;
         Book book = Book.builder().id(id).title("The Adventures").author("João").isbn("123").build();
         given(service.getById(id)).willReturn(Optional.of(book));

         // Act
         MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                 .get(BOOK_API.concat("/" + id))
                 .accept(MediaType.APPLICATION_JSON);

         // Assert
         mvc.perform(request)
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("id").value(1L))
                 .andExpect(jsonPath("title").value("The Adventures"))
                 .andExpect(jsonPath("author").value("João"))
                 .andExpect(jsonPath("isbn").value("123"));

    }

    @Test
    @DisplayName("Should return 404 when the requested book does not exist")
    public void  bookNotFound() throws Exception{
        // Arrange
        Long id = 1L;
        given(service.getById(anyLong())).willReturn(Optional.empty());

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete a book")
    public void deleteBook() throws Exception{
        // Arrange
        Long id = 1L;
        Book book = Book.builder().id(id).build();
        given(service.getById(id)).willReturn(Optional.of(book));

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting a nonexistent book")
    public void errorDeleteBook() throws Exception{
        // Arrange
        Long id = 1L;
        given(service.getById(id)).willReturn(Optional.empty());

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update a book")
    public void updateBook() throws Exception{
        // Arrange
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        Book book = Book.builder().id(id).title("Northern Samba").author("Maria").isbn("321").build();
        given(service.getById(id)).willReturn(Optional.of(book));
        Book bookUpdate = Book.builder().id(id).title("The Crystal Skull Society").author("Andréa del Fuego").isbn("001").build();
        given(service.update(book)).willReturn(bookUpdate);

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        // Assert
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value("001"));

    }
    @Test
    @DisplayName("Should return 404 when updating a nonexistent book")
    public void updateNonexistentBook() throws Exception{
        // Arrange
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        given(service.getById(anyLong())).willReturn(Optional.empty());


        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter books")
    public void findBooksTests() throws Exception{
        // Arrange
        Long id = 1L;
        Book book = Book.builder().id(id).title(createNewBook().getTitle()).author(createNewBook().getAuthor()).isbn(createNewBook().getIsbn()).build();
        given(service.find(any(Book.class), any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0,100),1));
        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }



    private BookDTO createNewBook() {
        return BookDTO.builder().title("The Crystal Skull Society").author("Andréa del Fuego").isbn("001").build();
    }













}
