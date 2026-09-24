package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LoanRepositoryTest {
    @Autowired LoanRepository repository;
    @Autowired TestEntityManager entityManager;

    @Test
    void combinesFiltersAndListsAllLoansWithoutFilters() {
        Loan loan = persistLoan();
        var page = PageRequest.of(0, 10);
        assertThat(repository.search(null, null, null, null, null, LocalDate.of(2026, 9, 24), page).getContent())
                .containsExactly(loan);
        assertThat(repository.search("123", "sam", loan.getMember().getId(), false, true,
                LocalDate.of(2026, 9, 24), page).getContent()).containsExactly(loan);
        assertThat(repository.search("different-isbn", "Sam", null, null, null,
                LocalDate.of(2026, 9, 24), page)).isEmpty();
    }

    @Test
    void dueTodayIsNotOverdueAndReturnedLoansAreExcluded() {
        Loan loan = persistLoan();
        Long memberId = loan.getMember().getId();
        assertThat(repository.existsByMemberIdAndReturnedFalseAndDueDateBefore(memberId,
                LocalDate.of(2026, 9, 23))).isFalse();
        assertThat(repository.existsByMemberIdAndReturnedFalseAndDueDateBefore(memberId,
                LocalDate.of(2026, 9, 24))).isTrue();
        assertThat(repository.countByMemberIdAndReturnedFalse(memberId)).isEqualTo(1);
        loan.setReturned(true);
        entityManager.flush();
        assertThat(repository.existsByMemberIdAndReturnedFalseAndDueDateBefore(memberId,
                LocalDate.of(2026, 9, 24))).isFalse();
        assertThat(repository.countByMemberIdAndReturnedFalse(memberId)).isZero();
    }

    private Loan persistLoan() {
        Book book = entityManager.persist(Book.builder().title("Title").author("Author").isbn("123").build());
        Member member = entityManager.persist(Member.builder().name("Sam").cardNumber("CARD-1")
                .email("sam@example.org").build());
        BookCopy copy = entityManager.persist(BookCopy.builder().book(book).barcode("COPY-1")
                .status(CopyStatus.ON_LOAN).build());
        return entityManager.persist(Loan.builder().book(book).member(member).copy(copy).customer("Sam")
                .loanDate(LocalDate.of(2026, 9, 9)).dueDate(LocalDate.of(2026, 9, 23)).build());
    }
}
