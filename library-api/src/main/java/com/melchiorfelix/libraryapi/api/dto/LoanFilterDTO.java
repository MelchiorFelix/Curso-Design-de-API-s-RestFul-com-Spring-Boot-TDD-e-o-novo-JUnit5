package com.melchiorfelix.libraryapi.api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanFilterDTO {
    private String isbn;
    private String customer;
    private Long memberId;
    private Boolean returned;
    private Boolean overdue;
}
