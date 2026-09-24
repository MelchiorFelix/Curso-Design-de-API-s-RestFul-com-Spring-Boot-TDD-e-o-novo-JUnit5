package com.melchiorfelix.libraryapi.service.impl;

import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.repository.BookRepository;
import com.melchiorfelix.libraryapi.model.repository.BookCopyRepository;
import com.melchiorfelix.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final BookCopyRepository copies;

    public BookServiceImpl(BookRepository repository, BookCopyRepository copies) {
        this.repository = repository;
        this.copies = copies;
    }


    @Override
    public Book save(Book book) {
        book.setId(null);
        book.setIsbn(book.getIsbn().trim());
        if(repository.existsByIsbn(book.getIsbn())){
            throw new BusinessException("ISBN already registered");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if(book == null || book.getId() == null) throw new IllegalArgumentException("Book ID cannot be null.");
        repository.findLockedById(book.getId());
        if (copies.existsByBookId(book.getId())) {
            throw new BusinessException("Books with inventory cannot be deleted; withdraw their copies instead");
        }
        this.repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if(book == null || book.getId() == null) throw new IllegalArgumentException("Book ID cannot be null.");
        return this.repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
        );
        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn) {
        return repository.findByIsbn(isbn);
    }
}
