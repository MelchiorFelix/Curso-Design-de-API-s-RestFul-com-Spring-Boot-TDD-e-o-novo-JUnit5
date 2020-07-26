package com.melchiorfelix.libraryapi.servvice;

import com.melchiorfelix.libraryapi.api.resource.BookController;
import com.melchiorfelix.libraryapi.model.entity.Loan;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);
}
