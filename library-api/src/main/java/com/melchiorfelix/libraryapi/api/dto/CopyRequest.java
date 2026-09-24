package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record CopyRequest(
        @Schema(example = "COPY-0001", description = "Unique, case-sensitive physical barcode")
        @NotBlank(message = "Barcode must not be blank")
        @Size(max = 80, message = "Barcode must be at most 80 characters") String barcode,
        @Schema(example = "FICTION-A1", description = "Optional shelf or storage location")
        @Size(max = 100, message = "Shelf location must be at most 100 characters") String shelfLocation) {
}
