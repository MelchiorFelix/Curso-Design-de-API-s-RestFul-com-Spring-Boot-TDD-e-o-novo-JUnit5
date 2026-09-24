package com.melchiorfelix.libraryapi.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Preserve the borrower's name at checkout when their profile changes.
    @Column(nullable = false)
    private String customer;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_book", nullable = false)
    private Book book;
    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(optional = false)
    @JoinColumn(name = "copy_id", nullable = false)
    private BookCopy copy;
    @Column(nullable = false)
    private LocalDate loanDate;
    @Column(nullable = false)
    private LocalDate dueDate;
    private LocalDate returnedDate;
    @Builder.Default
    @Column(nullable = false)
    private Boolean returned = false;
    @Column(nullable = false)
    private int renewalCount;
}
