package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.BookDTO;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.servvice.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService service;
    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody BookDTO dto){
        Book entity = modelMapper.map( dto, Book.class);
        entity = service.save(entity);
        return modelMapper.map( entity, BookDTO.class);
    }
}
