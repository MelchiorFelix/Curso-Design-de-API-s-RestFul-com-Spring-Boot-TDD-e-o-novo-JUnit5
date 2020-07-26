package com.melchiorfelix.libraryapi.servvice.impl;

import com.melchiorfelix.libraryapi.api.dto.LoanFilterDTO;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import com.melchiorfelix.libraryapi.model.repository.LoanRepository;
import com.melchiorfelix.libraryapi.servvice.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if(repository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable page) {
        return repository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), page);
    }
}
