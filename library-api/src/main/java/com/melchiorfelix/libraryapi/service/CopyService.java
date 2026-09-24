package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.api.dto.CopyRequest;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.*;
import com.melchiorfelix.libraryapi.model.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CopyService {
    private final BookRepository books;
    private final BookCopyRepository copies;

    public BookCopy create(Long bookId, CopyRequest request) {
        Book book = books.findLockedById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        String barcode = request.barcode().trim();
        if (copies.existsByBarcode(barcode)) {
            throw new BusinessException("Barcode already registered");
        }
        return copies.save(BookCopy.builder().book(book).barcode(barcode)
                .shelfLocation(request.shelfLocation() == null ? null : request.shelfLocation().trim()).build());
    }

    @Transactional(readOnly = true)
    public Page<BookCopy> find(Long bookId, CopyStatus status, Pageable pageable) {
        if (!books.existsById(bookId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return status == null ? copies.findByBookId(bookId, pageable)
                : copies.findByBookIdAndStatus(bookId, status, pageable);
    }

    public BookCopy withdraw(Long id) {
        Long bookId = copies.findBookId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Copy not found"));
        books.findLockedById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        BookCopy copy = copies.findById(id).orElseThrow();
        if (copy.getStatus() == CopyStatus.ON_LOAN) {
            throw new BusinessException("A copy on loan cannot be withdrawn");
        }
        copy.setStatus(CopyStatus.WITHDRAWN);
        return copy;
    }
}
