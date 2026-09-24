package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotBlank(message = "ISBN must not be blank") String isbn,
        @NotNull(message = "Member ID is required")
        @Positive(message = "Member ID must be positive") Long memberId,
        @Positive(message = "Copy ID must be positive") Long copyId) {
}
