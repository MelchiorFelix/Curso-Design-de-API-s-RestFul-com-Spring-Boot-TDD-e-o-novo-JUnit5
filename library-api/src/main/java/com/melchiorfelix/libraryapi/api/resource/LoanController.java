package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.LoanDTO;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import com.melchiorfelix.libraryapi.servvice.BookService;
import com.melchiorfelix.libraryapi.servvice.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto){
        Book book = bookService.getBookByIsbn(dto.getIsbn()).get();
        Loan loan = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();
        loan = service.save(loan);

        return loan.getId();
    }

}
