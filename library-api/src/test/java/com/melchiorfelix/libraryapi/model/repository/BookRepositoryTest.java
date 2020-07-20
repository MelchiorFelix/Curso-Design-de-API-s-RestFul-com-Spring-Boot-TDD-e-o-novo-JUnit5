package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;
    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando exisitir um livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenario
        String isbn = "123";
        Book book = Book.builder().title("As aventuras").author("Joao").isbn(isbn).build();
        entityManager.persist(book);
        //execucao
        boolean exists = repository.existsByIsbn(isbn);

        //verificao
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDoesntExists(){
        //cenario
        String isbn = "123";

        //execucao
        boolean exists = repository.existsByIsbn(isbn);

        //verificao
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findById(){
        //cenario
        Book book = newBook();
        entityManager.persist(book);
        //execucao
        Optional<Book> foundBook = repository.findById(book.getId());

        //verificao
        assertThat(foundBook.isPresent()).isTrue();
    }

    private Book newBook() {
        return Book.builder().title("As aventuras").author("Joao").isbn("123").build();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBook(){

        Book book = newBook();

        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBook(){
        //cenario
        Book book = newBook();
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        //execução
        repository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        //verficação
        assertThat(deletedBook).isNull();


    }
}
