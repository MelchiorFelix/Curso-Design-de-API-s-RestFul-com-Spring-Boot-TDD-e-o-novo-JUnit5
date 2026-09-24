package com.melchiorfelix.libraryapi.api.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotBlank(message = "Title must not be empty")
    private String title;

    @NotBlank(message = "Author must not be empty")
    private String author;

    @NotBlank(message = "ISBN must not be empty")
    private String isbn;
}
