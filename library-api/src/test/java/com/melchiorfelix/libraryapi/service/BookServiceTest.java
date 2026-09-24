package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.repository.BookRepository;
import com.melchiorfelix.libraryapi.model.repository.BookCopyRepository;
import com.melchiorfelix.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.hibernate.validator.constraints.time.DurationMax;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {


    BookService service;
    @MockitoBean
    BookRepository repository;
    @MockitoBean
    BookCopyRepository copies;


    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository, copies);
    }

    @Test
    @DisplayName("Should save a book")
    public void saveBookTest(){
        // Arrange
        Book book = newBook();
        when(repository.save(book)).thenReturn(Book.builder().id(1L).author("Alex Smith").title("The Adventures").isbn("123").build());

        // Act
        Book savedBook = service.save(book);

        // Assert
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getAuthor()).isEqualTo("Alex Smith");
        assertThat(savedBook.getTitle()).isEqualTo("The Adventures");
    }



    @Test
    @DisplayName("Should reject a book with a duplicate ISBN")
    public void shouldNotSaveABookWithDuplicatedISBN(){
        // Arrange
        Book book = newBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        // Act
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // Assert
        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("ISBN already registered");

        verify(repository, never()).save(book);

    }

    @Test
    @DisplayName("Should retrieve a book by ID")
    public void getById(){
        // Arrange
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        // Act
        Optional<Book> foundBook = service.getById(id);

        // Assert
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(newBook().getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(newBook().getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(newBook().getIsbn());

    }
    @Test
    @DisplayName("Should return an empty result when the book does not exist")
    public void getNonexistentBookById(){
        // Arrange
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Book> book = service.getById(id);

        // Assert
        assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete a book by ID")
    public void deleteBook(){
        // Arrange
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);


        // Act
        assertDoesNotThrow(() -> service.delete(book));

        // Assert
        verify(repository, times(1)).delete(book);

    }

    @Test
    @DisplayName("Should reject deletion of a book without an ID")
    public void rejectDeletingBookWithoutId(){
        // Arrange
        Book book  = newBook();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> service.delete(book));
        verify(repository, never()).delete(book);
    }

    @Test
    @DisplayName("Should update a book")
    public void updateBook(){
        // Arrange
        Long id = 1L;
        Book book  = newBook();
        book.setId(id);
        when(repository.save(book)).thenReturn(book);

        // Act
        Book updatedBook = service.update(book);

        // Assert
        assertThat(updatedBook.getId()).isEqualTo(book.getId());
        verify(repository, times(1)).save(book);

    }

    @Test
    @DisplayName("Should reject updating a book without an ID")
    public void rejectUpdatingBookWithoutId(){
        // Arrange
        Book book  = newBook();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> service.update(book));
        verify(repository, never()).save(book);
    }

    @Test
    @DisplayName("Should filter books by their properties")
    public void findBookTest(){
        // Arrange
        Book book = newBook();
        PageRequest pageRe = PageRequest.of(0, 10);
        PageImpl<Book> page = new PageImpl<>(Arrays.asList(book), pageRe, 1);
        when(repository.findAll(any(Example.class), any(PageRequest.class))).thenReturn(page);

        // Act
        Page<Book> result = service.find(book, pageRe);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(Arrays.asList(book));
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);



    }

    @Test
    @DisplayName("Should retrieve a book by ISBN")
    public void getBookByIsbn() {
        // Arrange
        String isbn = "123";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        // Act
        Optional<Book> bookByIsbn = service.getBookByIsbn(isbn);

        // Assert
        assertThat(bookByIsbn.isPresent()).isTrue();
        assertThat(bookByIsbn.get().getId()).isEqualTo(1L);
        assertThat(bookByIsbn.get().getIsbn()).isEqualTo(isbn);
        verify(repository, times(1)).findByIsbn(isbn);
    }

    public static Book newBook() {
        return Book.builder().isbn("123").author("Alex Smith").title("The Adventures").build();
    }
}
