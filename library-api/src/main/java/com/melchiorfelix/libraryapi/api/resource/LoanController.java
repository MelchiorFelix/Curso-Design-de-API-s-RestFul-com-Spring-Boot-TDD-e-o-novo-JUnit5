package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@Valid @RequestBody CheckoutRequest request) {
        return service.checkout(request).getId();
    }

    @GetMapping("{id}")
    public LoanDTO get(@PathVariable Long id) {
        return LoanDTO.from(service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found")));
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @Valid @RequestBody ReturnedLoanDTO request) {
        service.returnLoan(id);
    }

    @PostMapping("{id}/renew")
    public LoanDTO renew(@PathVariable Long id) {
        return LoanDTO.from(service.renew(id));
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO filter, Pageable pageable) {
        return service.find(filter, pageable).map(LoanDTO::from);
    }

    @GetMapping("overdue")
    public Page<LoanDTO> overdue(Pageable pageable) {
        return service.find(LoanFilterDTO.builder().overdue(true).build(), pageable).map(LoanDTO::from);
    }
}
