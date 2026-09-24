package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.api.dto.LoanFilterDTO;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import com.melchiorfelix.libraryapi.model.repository.LoanRepository;
import com.melchiorfelix.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockitoBean
    private LoanRepository repository;

    private LoanService service;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Should save a loan")
    public void saveLoanTest(){
        // Arrange
        Book book = Book.builder().id(1L).build();
        String customer = "João";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book).build();

        when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        when(repository.save(savingLoan)).thenReturn(savedLoan);

        // Act
        Loan loan = service.save(savingLoan);

        // Assert
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook()).isEqualTo(savedLoan.getBook());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }


    @Test
    @DisplayName("Should reject a loan when the book is already on loan")
    public void loanedBookSaveTest(){
        // Arrange
        Book book = Book.builder().id(1L).build();
        String customer = "João";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
        when(repository.existsByBookAndNotReturned(book)).thenReturn(true);



        // Act
        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        verify(repository, never()).save(savingLoan);

    }

    @Test
    @DisplayName("Should retrieve loan details by ID")
    public void getLoanDetails(){
        // Arrange
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(loan));

        // Act
        Optional<Loan> result = service.getById(id);

        // Assert
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Should update a loan")
    public void updateLoan(){
        // Arrange
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);
        when(repository.save(loan)).thenReturn(loan);

        // Act
        Loan update = service.update(loan);

        // Assert
        assertThat(update.getReturned()).isTrue();
        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Should filter loans by their properties")
    public void findLoanTest(){
        // Arrange
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Alex Smith").isbn("321").build();
        Loan loan = createLoan();
        loan.setId(1L);
        PageRequest pageRe = PageRequest.of(0, 10);
        PageImpl<Loan> page = new PageImpl<>(Arrays.asList(loan), pageRe, 1);
        when(repository.findByBookIsbnOrCustomer(anyString(), anyString(), any(PageRequest.class))).thenReturn(page);

        // Act
        Page<Loan> result = service.find(loanFilterDTO, pageRe);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(Arrays.asList(loan));
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);



    }





    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();
        String customer = "João";

        return Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }
}
