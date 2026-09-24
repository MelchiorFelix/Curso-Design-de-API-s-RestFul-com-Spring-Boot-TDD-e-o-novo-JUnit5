package com.melchiorfelix.libraryapi.api.resource;

import tools.jackson.databind.ObjectMapper;
import com.melchiorfelix.libraryapi.api.dto.LoanDTO;
import com.melchiorfelix.libraryapi.api.dto.LoanFilterDTO;
import com.melchiorfelix.libraryapi.api.dto.ReturnedLoanDTO;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import com.melchiorfelix.libraryapi.service.BookService;
import com.melchiorfelix.libraryapi.service.LoanService;
import com.melchiorfelix.libraryapi.service.LoanServiceTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private LoanService loanService;

    @Test
    @DisplayName("Should create a loan")
    public void createLoan() throws Exception{
        // Arrange
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Maria").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn("123").build();
        given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1L).customer("Maria").book(book).loanDate(LocalDate.now()).build();
        given(loanService.save(any(Loan.class))).willReturn(loan);

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Assert
        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Should reject a loan when the ISBN does not match a book")
    public void invalidIsbnLoan() throws Exception{
        // Arrange
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Maria").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Assert
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1) ))
                .andExpect(jsonPath("errors[0]").value("Book not found for the provided ISBN"));
    }

    @Test
    @DisplayName("Should reject a loan when the book is already on loan")
    public void rejectLoanForBookAlreadyOnLoan() throws Exception{
        // Arrange
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Maria").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1L).isbn("123").build();
        given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

        given(loanService.save(any(Loan.class))).willThrow(new BusinessException("Book already loaned"));

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Assert
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors",Matchers.hasSize(1) ))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Should return a book")
    public void returnBookTest() throws Exception{
        // Arrange { returned: true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        Loan loan = Loan.builder().id(1L).build();
        given(loanService.getById(anyLong())).willReturn(Optional.of(loan));


        String json = new ObjectMapper().writeValueAsString(dto);

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk());

        verify(loanService, times(1)).update(loan);

    }

    @Test
    @DisplayName("Should return 404 when returning a book for a nonexistent loan")
    public void returnInexistentBookTest() throws Exception{
        // Arrange { returned: true }
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
        given(loanService.getById(anyLong())).willReturn(Optional.empty());


        String json = new ObjectMapper().writeValueAsString(dto);

        mvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isNotFound());


    }

    @Test
    @DisplayName("Should filter loans")
    public void findLoanTests() throws Exception{
        // Arrange
        Long id = 1L;
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(id);
        loan.setBook(Book.builder().id(1L).isbn("321").build());

        given(loanService.find(any(LoanFilterDTO.class), any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0,10),1));

        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", loan.getBook().getIsbn(), loan.getCustomer());

        // Act
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Assert
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0));

    }



}
