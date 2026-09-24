package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.*;
import com.melchiorfelix.libraryapi.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean LoanService service;

    @Test
    void checkoutKeepsTheCreatedLoanIdResponse() throws Exception {
        when(service.checkout(any())).thenReturn(loan());
        mvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"123\",\"memberId\":1}"))
                .andExpect(status().isCreated()).andExpect(content().string("4"));
        verify(service).checkout(new CheckoutRequest("123", 1L, null));
    }

    @Test
    void rejectsMissingMemberAndBlankIsbnBeforeCallingService() throws Exception {
        mvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors.length()").value(2));
        verifyNoInteractions(service);
    }

    @Test
    void reportsUnavailableCopies() throws Exception {
        when(service.checkout(any())).thenThrow(new BusinessException("No available copies for this book"));
        mvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"123\",\"memberId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0]").value("No available copies for this book"));
    }

    @Test
    void returnsLoan() throws Exception {
        mvc.perform(patch("/api/loans/4").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"returned\":true}")).andExpect(status().isOk());
        verify(service).returnLoan(4L);
    }

    @Test
    void rejectsReopeningAndMissingReturnStatus() throws Exception {
        for (String body : List.of("{}", "{\"returned\":false}")) {
            mvc.perform(patch("/api/loans/4").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void returns404ForUnknownLoan() throws Exception {
        when(service.returnLoan(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        mvc.perform(patch("/api/loans/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"returned\":true}")).andExpect(status().isNotFound());
    }

    @Test
    void exposesDatesCopyAndMemberInLoanDetails() throws Exception {
        when(service.getById(4L)).thenReturn(Optional.of(loan()));
        mvc.perform(get("/api/loans/4")).andExpect(status().isOk())
                .andExpect(jsonPath("memberId").value(1)).andExpect(jsonPath("barcode").value("COPY-1"))
                .andExpect(jsonPath("dueDate").value("2026-10-08"));
    }

    @Test
    void routesOverdueReportToOverdueFilter() throws Exception {
        when(service.find(any(), any())).thenReturn(new PageImpl<>(List.of(loan())));
        mvc.perform(get("/api/loans/overdue")).andExpect(status().isOk())
                .andExpect(jsonPath("content[0].copyId").value(3));
        verify(service).find(eq(LoanFilterDTO.builder().overdue(true).build()), any(Pageable.class));
    }

    private Loan loan() {
        Book book = Book.builder().id(2L).title("Title").author("Author").isbn("123").build();
        return Loan.builder().id(4L).book(book).member(Member.builder().id(1L).build())
                .copy(BookCopy.builder().id(3L).book(book).barcode("COPY-1").build())
                .customer("Sam").loanDate(LocalDate.of(2026, 9, 24))
                .dueDate(LocalDate.of(2026, 10, 8)).build();
    }
}
