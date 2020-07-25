package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.melchiorfelix.libraryapi.servvice.BookServiceTest.newBook;
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
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
    public void existsByBookAndNotReturned(){
        //cenario
        Book book = newBook();
        entityManager.persist(book);
        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();

        entityManager.persist(loan);

        //execucao
        boolean exists = repository.existsByBookAndNotReturned(book);

        //verificacao
        assertThat(exists).isTrue();

    }


}
