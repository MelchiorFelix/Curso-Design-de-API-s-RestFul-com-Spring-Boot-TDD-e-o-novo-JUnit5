package com.melchiorfelix.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanFilterDTO {
    private String isbn;
    private String customer;
}
