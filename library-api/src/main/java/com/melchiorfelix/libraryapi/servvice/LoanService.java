package com.melchiorfelix.libraryapi.servvice;

import com.melchiorfelix.libraryapi.api.dto.LoanFilterDTO;
import com.melchiorfelix.libraryapi.api.resource.BookController;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable page);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);
}
