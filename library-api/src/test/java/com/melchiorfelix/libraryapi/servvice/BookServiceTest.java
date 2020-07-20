package com.melchiorfelix.libraryapi.servvice;

import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.repository.BookRepository;
import com.melchiorfelix.libraryapi.servvice.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {


    BookService service;
    @MockBean
    BookRepository repository;


    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest(){
        //cenario
        Book book = newBook();
        when(repository.save(book)).thenReturn(Book.builder().id(1L).author("Fulano").title("As aventuras").isbn("123").build());

        //excecucao
        Book savedBook = service.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
    }



    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        //cenario
        Book book = newBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        //execucao
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        //verificacao
        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Isbn já cadastrado");

        verify(repository, never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getById(){
        //cenario
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //verificacao
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(newBook().getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(newBook().getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(newBook().getIsbn());

    }
    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe")
    public void errorgetById(){
        //cenario
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        //execucao
        Optional<Book> book = service.getById(id);

        //verificacao
        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro por id")
    public void deleteteBook(){
        //cenario
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);


        //execucao
        assertDoesNotThrow(() -> service.delete(book));

        //verificacao
        verify(repository, times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve gerar erro ao tentar deletar um livro que não existe")
    public void erroDeleteBook(){
        //cenario
        Book book  = newBook();

        //verificacao
        assertThrows(IllegalArgumentException.class, () -> service.delete(book));
        verify(repository, never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBook(){
        //cenario
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);
        when(repository.save(book)).thenReturn(book);

        //execucao
        Book atualizado = service.update(book);

        //verificacao
        assertThat(atualizado.getId()).isEqualTo(book.getId());
        verify(repository, times(1)).save(book);

    }

    @Test
    @DisplayName("Deve gerar erro ao tentar atualizar um livro que não existe")
    public void erroUpdateBok(){
        //cenario
        Book book  = newBook();

        //verificacao
        assertThrows(IllegalArgumentException.class, () -> service.update(book));
        verify(repository, never()).save(book);
    }

    private Book newBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }
}
