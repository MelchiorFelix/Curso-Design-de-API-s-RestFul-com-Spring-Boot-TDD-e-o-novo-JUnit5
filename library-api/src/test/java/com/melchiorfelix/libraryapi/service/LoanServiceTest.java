package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.api.dto.CheckoutRequest;
import com.melchiorfelix.libraryapi.config.CirculationPolicy;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.*;
import com.melchiorfelix.libraryapi.model.repository.*;
import com.melchiorfelix.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    @Mock LoanRepository loans;
    @Mock MemberRepository members;
    @Mock BookRepository books;
    @Mock BookCopyRepository copies;
    CirculationPolicy policy;
    LoanService service;
    final Clock clock = Clock.fixed(Instant.parse("2026-09-24T12:00:00Z"), ZoneOffset.UTC);
    Member member;
    Book book;
    BookCopy copy;

    @BeforeEach
    void setUp() {
        policy = new CirculationPolicy();
        service = new LoanServiceImpl(loans, members, books, copies, policy, clock);
        member = Member.builder().id(1L).name("Sam").build();
        book = Book.builder().id(2L).isbn("123").build();
        copy = BookCopy.builder().id(3L).book(book).barcode("COPY-1").build();
    }

    @Test
    void checkoutSetsServerControlledDatesAndBorrower() {
        when(members.findLockedById(1L)).thenReturn(Optional.of(member));
        when(books.findLockedByIsbn("123")).thenReturn(Optional.of(book));
        when(copies.findFirstByBookIdAndStatusOrderByIdAsc(2L, CopyStatus.AVAILABLE)).thenReturn(Optional.of(copy));
        when(loans.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Loan loan = service.checkout(new CheckoutRequest("123", 1L, null));
        assertThat(loan.getLoanDate()).isEqualTo(LocalDate.of(2026, 9, 24));
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 8));
        assertThat(loan.getCustomer()).isEqualTo("Sam");
        assertThat(loan.getReturned()).isFalse();
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.ON_LOAN);
    }

    @Test
    void suspendedMemberCannotBorrow() {
        member.setActive(false);
        when(members.findLockedById(1L)).thenReturn(Optional.of(member));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest("123", 1L, null)))
                .isInstanceOf(BusinessException.class).hasMessage("Member is suspended");
        verifyNoInteractions(books, copies);
    }

    @Test
    void memberLimitIsCheckedBeforeAllocatingInventory() {
        when(members.findLockedById(1L)).thenReturn(Optional.of(member));
        when(loans.countByMemberIdAndReturnedFalse(1L)).thenReturn(5L);
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest("123", 1L, null)))
                .hasMessage("Member has reached the active loan limit");
        verify(loans, never()).save(any());
    }

    @Test
    void cannotSelectACopyOfAnotherTitle() {
        when(members.findLockedById(1L)).thenReturn(Optional.of(member));
        when(books.findLockedByIsbn("123")).thenReturn(Optional.of(book));
        copy.setBook(Book.builder().id(9L).build());
        when(copies.findById(3L)).thenReturn(Optional.of(copy));
        assertThatThrownBy(() -> service.checkout(new CheckoutRequest("123", 1L, 3L)))
                .hasMessage("Copy does not belong to the requested book");
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    @Test
    void zeroRenewalsDisablesRenewal() {
        policy.setMaxRenewals(0);
        Loan loan = prepareLockedLoan();
        assertThatThrownBy(() -> service.renew(4L)).hasMessage("Renewal limit reached");
        assertThat(loan.getRenewalCount()).isZero();
    }

    @Test
    void renewalExtendsExistingDueDate() {
        Loan loan = prepareLockedLoan();
        service.renew(4L);
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 22));
        assertThat(loan.getRenewalCount()).isEqualTo(1);
    }

    @Test
    void repeatedReturnDoesNotReleaseACopyAlreadyBorrowedAgain() {
        Loan loan = prepareLockedLoan();
        loan.setReturned(true);
        loan.setReturnedDate(LocalDate.of(2026, 9, 23));
        copy.setStatus(CopyStatus.ON_LOAN);
        service.returnLoan(4L);
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.ON_LOAN);
        assertThat(loan.getReturnedDate()).isEqualTo(LocalDate.of(2026, 9, 23));
    }

    private Loan prepareLockedLoan() {
        Loan loan = Loan.builder().id(4L).book(book).member(member).copy(copy)
                .loanDate(LocalDate.of(2026, 9, 24)).dueDate(LocalDate.of(2026, 10, 8)).build();
        when(loans.findMemberId(4L)).thenReturn(Optional.of(1L));
        when(members.findLockedById(1L)).thenReturn(Optional.of(member));
        when(loans.findBookId(4L)).thenReturn(Optional.of(2L));
        when(books.findLockedById(2L)).thenReturn(Optional.of(book));
        when(loans.findById(4L)).thenReturn(Optional.of(loan));
        return loan;
    }
}
