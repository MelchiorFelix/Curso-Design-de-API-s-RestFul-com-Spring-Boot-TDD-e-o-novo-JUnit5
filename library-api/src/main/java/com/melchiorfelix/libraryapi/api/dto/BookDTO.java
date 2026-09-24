package com.melchiorfelix.libraryapi.api.dto;

import lombok.*;

import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotEmpty(message = "Title must not be empty")
    private String title;

    @NotEmpty(message = "Author must not be empty")
    private String author;

    @NotEmpty(message = "ISBN must not be empty")
    private String isbn;
}
