package com.melchior.cursos.libraryapi.api.resource;

import com.melchior.cursos.libraryapi.api.dto.BookDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(){
        BookDTO dto = new BookDTO();
        dto.setTitle("Meu Livro");
        dto.setAuthor("Autor");
        dto.setIsbn("1213212");
        dto.setId((long) 11);
        return  dto;
    }
}
