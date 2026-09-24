package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.*;

public record MemberRequest(
        @NotBlank(message = "Card number must not be blank")
        @Size(max = 50, message = "Card number must be at most 50 characters") String cardNumber,
        @NotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters") String name,
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must be at most 254 characters") String email) {
}
