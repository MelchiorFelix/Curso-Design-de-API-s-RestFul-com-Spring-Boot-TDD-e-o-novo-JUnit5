package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByMemberIdAndReturnedFalse(Long memberId);
    boolean existsByMemberIdAndReturnedFalseAndDueDateBefore(Long memberId, LocalDate date);
    Page<Loan> findByBook(Book book, Pageable pageable);
    Page<Loan> findByMemberId(Long memberId, Pageable pageable);

    @Query("select l.member.id from Loan l where l.id = :id")
    Optional<Long> findMemberId(@Param("id") Long id);

    @Query("select l.book.id from Loan l where l.id = :id")
    Optional<Long> findBookId(@Param("id") Long id);

    @Query("""
            select l from Loan l where
            (:isbn is null or l.book.isbn = :isbn) and
            (:customer is null or lower(l.customer) = lower(:customer)) and
            (:memberId is null or l.member.id = :memberId) and
            (:returned is null or l.returned = :returned) and
            (:overdue is null or
             (:overdue = true and l.returned = false and l.dueDate < :today) or
             (:overdue = false and (l.returned = true or l.dueDate >= :today)))
            """)
    Page<Loan> search(@Param("isbn") String isbn, @Param("customer") String customer,
                      @Param("memberId") Long memberId, @Param("returned") Boolean returned,
                      @Param("overdue") Boolean overdue, @Param("today") LocalDate today,
                      Pageable pageable);
}
