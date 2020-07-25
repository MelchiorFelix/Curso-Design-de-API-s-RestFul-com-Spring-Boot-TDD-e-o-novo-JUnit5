package com.melchiorfelix.libraryapi.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Loan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customer;
    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;
    private LocalDate loanDate;
    private Boolean returned;
}
