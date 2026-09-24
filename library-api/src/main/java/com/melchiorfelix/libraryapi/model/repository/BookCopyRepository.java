package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    @Query("select c.book.id from BookCopy c where c.id = :id")
    Optional<Long> findBookId(@Param("id") Long id);
    boolean existsByBarcode(String barcode);
    boolean existsByBookId(Long bookId);
    Optional<BookCopy> findFirstByBookIdAndStatusOrderByIdAsc(Long bookId, CopyStatus status);
    Page<BookCopy> findByBookId(Long bookId, Pageable pageable);
    Page<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status, Pageable pageable);
}
