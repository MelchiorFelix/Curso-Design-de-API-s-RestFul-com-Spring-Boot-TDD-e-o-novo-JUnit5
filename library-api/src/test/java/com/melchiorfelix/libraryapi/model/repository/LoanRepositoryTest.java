package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.melchiorfelix.libraryapi.service.BookServiceTest.newBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should detect an outstanding loan for the book")
    public void existsByBookAndNotReturned(){
        // Arrange
        Loan loan = createAndPersistLoan();

        // Act
        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());

        // Assert
        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Should find loans by book ISBN or customer")
    public void findByBookIsbnOrCustomer(){
        // Arrange
        Loan loan = createAndPersistLoan();

        // Act
        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Alex Smith", PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    public Loan createAndPersistLoan(){
        Book book = newBook();
        entityManager.persist(book);
        Loan loan = Loan.builder().book(book).customer("Alex Smith").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        return loan;
    }
}
