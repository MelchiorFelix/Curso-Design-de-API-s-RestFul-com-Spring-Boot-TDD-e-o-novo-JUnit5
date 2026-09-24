package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record CheckoutRequest(
        @Schema(example = "9780000000001", description = "ISBN of a registered book with available copies")
        @NotBlank(message = "ISBN must not be blank") String isbn,
        @Schema(example = "1", description = "ID returned by member registration")
        @NotNull(message = "Member ID is required")
        @Positive(message = "Member ID must be positive") Long memberId,
        @Schema(example = "1", description = "Optional physical copy ID; omit to select an available copy automatically")
        @Positive(message = "Copy ID must be positive") Long copyId) {
}
