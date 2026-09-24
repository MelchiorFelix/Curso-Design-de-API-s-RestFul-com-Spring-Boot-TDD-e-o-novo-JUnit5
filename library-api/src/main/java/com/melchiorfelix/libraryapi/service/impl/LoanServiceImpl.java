package com.melchiorfelix.libraryapi.service.impl;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.config.CirculationPolicy;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.*;
import com.melchiorfelix.libraryapi.model.repository.*;
import com.melchiorfelix.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {
    private final LoanRepository repository;
    private final MemberRepository members;
    private final BookRepository books;
    private final BookCopyRepository copies;
    private final CirculationPolicy policy;
    private final Clock clock;

    @Override
    public Loan checkout(CheckoutRequest request) {
        // Lock member before book to serialize member limits and inventory changes.
        Member member = lockMember(request.memberId());
        requireActive(member);
        LocalDate today = LocalDate.now(clock);
        if (repository.existsByMemberIdAndReturnedFalseAndDueDateBefore(member.getId(), today)) {
            throw new BusinessException("Member has overdue loans");
        }
        if (repository.countByMemberIdAndReturnedFalse(member.getId()) >= policy.getMaxActiveLoans()) {
            throw new BusinessException("Member has reached the active loan limit");
        }
        Book book = books.findLockedByIsbn(request.isbn().trim())
                .orElseThrow(() -> new BusinessException("Book not found for the provided ISBN"));
        BookCopy copy = request.copyId() == null
                ? copies.findFirstByBookIdAndStatusOrderByIdAsc(book.getId(), CopyStatus.AVAILABLE)
                    .orElseThrow(() -> new BusinessException("No available copies for this book"))
                : copies.findById(request.copyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copy not found"));
        if (!copy.getBook().getId().equals(book.getId())) {
            throw new BusinessException("Copy does not belong to the requested book");
        }
        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BusinessException("Copy is not available");
        }
        copy.setStatus(CopyStatus.ON_LOAN);
        return repository.save(Loan.builder().book(book).copy(copy).member(member)
                .customer(member.getName()).loanDate(today).dueDate(today.plusDays(policy.getLoanDays()))
                .returned(false).renewalCount(0).build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan returnLoan(Long id) {
        Loan loan = lockLoan(id);
        if (!Boolean.TRUE.equals(loan.getReturned())) {
            loan.setReturned(true);
            loan.setReturnedDate(LocalDate.now(clock));
            loan.getCopy().setStatus(CopyStatus.AVAILABLE);
        }
        return loan;
    }

    @Override
    public Loan renew(Long id) {
        Loan loan = lockLoan(id);
        requireActive(loan.getMember());
        if (Boolean.TRUE.equals(loan.getReturned())) {
            throw new BusinessException("Returned loans cannot be renewed");
        }
        LocalDate today = LocalDate.now(clock);
        if (repository.existsByMemberIdAndReturnedFalseAndDueDateBefore(loan.getMember().getId(), today)) {
            throw new BusinessException("Member has overdue loans");
        }
        if (loan.getRenewalCount() >= policy.getMaxRenewals()) {
            throw new BusinessException("Renewal limit reached");
        }
        loan.setDueDate(loan.getDueDate().plusDays(policy.getRenewalDays()));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        return loan;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Loan> find(LoanFilterDTO filter, Pageable page) {
        return repository.search(blankToNull(filter.getIsbn()), blankToNull(filter.getCustomer()),
                filter.getMemberId(), filter.getReturned(), filter.getOverdue(), LocalDate.now(clock), page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Loan> getLoansByMember(Long memberId, Pageable pageable) {
        return repository.findByMemberId(memberId, pageable);
    }

    private Member lockMember(Long id) {
        return members.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    private Loan lockLoan(Long id) {
        // Scalar reads avoid caching stale entities while another transaction holds the locks.
        Long memberId = repository.findMemberId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        lockMember(memberId);
        Long bookId = repository.findBookId(id).orElseThrow();
        books.findLockedById(bookId).orElseThrow();
        return repository.findById(id).orElseThrow();
    }

    private void requireActive(Member member) {
        if (!member.isActive()) {
            throw new BusinessException("Member is suspended");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
