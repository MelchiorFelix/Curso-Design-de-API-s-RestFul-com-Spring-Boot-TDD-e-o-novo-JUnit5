package com.melchiorfelix.libraryapi.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    @Column(nullable = false, unique = true, length = 80)
    private String barcode;
    @Column(length = 100)
    private String shelfLocation;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status = CopyStatus.AVAILABLE;
}
