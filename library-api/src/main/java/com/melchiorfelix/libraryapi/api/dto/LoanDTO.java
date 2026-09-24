package com.melchiorfelix.libraryapi.api.dto;

import com.melchiorfelix.libraryapi.model.entity.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private Long id;
    private String isbn;
    private String customer;
    private BookDTO book;
    private Long memberId;
    private Long copyId;
    private String barcode;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;
    private Boolean returned;
    private int renewalCount;

    public static LoanDTO from(Loan loan) {
        Book book = loan.getBook();
        return LoanDTO.builder().id(loan.getId()).isbn(book.getIsbn()).customer(loan.getCustomer())
                .book(BookDTO.builder().id(book.getId()).title(book.getTitle())
                        .author(book.getAuthor()).isbn(book.getIsbn()).build())
                .memberId(loan.getMember().getId()).copyId(loan.getCopy().getId())
                .barcode(loan.getCopy().getBarcode()).loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate()).returnedDate(loan.getReturnedDate())
                .returned(loan.getReturned()).renewalCount(loan.getRenewalCount()).build();
    }
}
