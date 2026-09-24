package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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
    @DisplayName("Should return true when a book with the ISBN exists")
    public void returnTrueWhenIsbnExists(){
        // Arrange
        String isbn = "123";
        Book book = Book.builder().title("The Adventures").author("Joao").isbn(isbn).build();
        entityManager.persist(book);
        // Act
        boolean exists = repository.existsByIsbn(isbn);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when no book with the ISBN exists")
    public void returnFalseWhenIsbnDoesntExists(){
        // Arrange
        String isbn = "123";

        // Act
        boolean exists = repository.existsByIsbn(isbn);

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should retrieve a book by ID")
    public void findById(){
        // Arrange
        Book book = newBook();
        entityManager.persist(book);
        // Act
        Optional<Book> foundBook = repository.findById(book.getId());

        // Assert
        assertThat(foundBook.isPresent()).isTrue();
    }

    private Book newBook() {
        return Book.builder().title("The Adventures").author("Joao").isbn("123").build();
    }

    @Test
    @DisplayName("Should save a book")
    public void saveBook(){

        Book book = newBook();

        Book savedBook = repository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should delete a book")
    public void deleteBook(){
        // Arrange
        Book book = newBook();
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());

        // Act
        repository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        // Assert
        assertThat(deletedBook).isNull();


    }
}
