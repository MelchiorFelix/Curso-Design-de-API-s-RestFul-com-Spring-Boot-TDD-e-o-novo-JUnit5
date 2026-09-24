package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CopyRequest(
        @NotBlank(message = "Barcode must not be blank")
        @Size(max = 80, message = "Barcode must be at most 80 characters") String barcode,
        @Size(max = 100, message = "Shelf location must be at most 100 characters") String shelfLocation) {
}
