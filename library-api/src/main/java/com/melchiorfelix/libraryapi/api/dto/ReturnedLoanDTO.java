package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReturnedLoanDTO {
    @NotNull(message = "Returned status is required")
    @AssertTrue(message = "Only returning a loan is supported; create a new loan to borrow again")
    private Boolean returned;
}
