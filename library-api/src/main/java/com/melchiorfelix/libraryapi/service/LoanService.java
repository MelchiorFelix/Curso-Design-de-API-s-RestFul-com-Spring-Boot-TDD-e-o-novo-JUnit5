package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.model.entity.*;
import org.springframework.data.domain.*;
import java.util.Optional;

public interface LoanService {
    Loan checkout(CheckoutRequest request);
    Optional<Loan> getById(Long id);
    Loan returnLoan(Long id);
    Loan renew(Long id);
    Page<Loan> find(LoanFilterDTO filter, Pageable page);
    Page<Loan> getLoansByBook(Book book, Pageable pageable);
    Page<Loan> getLoansByMember(Long memberId, Pageable pageable);
}
