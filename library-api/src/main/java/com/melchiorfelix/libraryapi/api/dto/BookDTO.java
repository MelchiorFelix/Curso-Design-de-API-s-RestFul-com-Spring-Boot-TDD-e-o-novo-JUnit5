package com.melchiorfelix.libraryapi.api.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Title must not be empty")
    @Schema(example = "The Adventures")
    private String title;

    @NotBlank(message = "Author must not be empty")
    @Schema(example = "Alex Smith")
    private String author;

    @NotBlank(message = "ISBN must not be empty")
    @Schema(example = "9780000000001", description = "Unique catalog identifier; unchanged by book updates")
    private String isbn;
}
