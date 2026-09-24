package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReturnedLoanDTO {
    @Schema(example = "true", allowableValues = {"true"}, description = "Must be true; loans cannot be reopened")
    @NotNull(message = "Returned status is required")
    @AssertTrue(message = "Only returning a loan is supported; create a new loan to borrow again")
    private Boolean returned;
}
