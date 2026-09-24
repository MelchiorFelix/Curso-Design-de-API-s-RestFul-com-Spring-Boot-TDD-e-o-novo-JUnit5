package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberRequest(
        @Schema(example = "LIB-0001", description = "Unique card number; trimmed and normalized to uppercase")
        @NotBlank(message = "Card number must not be blank")
        @Size(max = 50, message = "Card number must be at most 50 characters") String cardNumber,
        @Schema(example = "Sam Taylor")
        @NotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters") String name,
        @Schema(example = "sam@example.org")
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email must be at most 254 characters") String email) {
}
