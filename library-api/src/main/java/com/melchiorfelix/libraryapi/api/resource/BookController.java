package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.BookDTO;
import com.melchiorfelix.libraryapi.api.dto.LoanDTO;
import com.melchiorfelix.libraryapi.model.entity.Book;
import com.melchiorfelix.libraryapi.model.entity.Loan;
import com.melchiorfelix.libraryapi.service.BookService;
import com.melchiorfelix.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Books", description = "Catalog records and borrowing history")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createBook", summary = "Create a book",
            description = "Create a catalog record. Register physical copies separately before lending.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public BookDTO create(@RequestBody @Valid BookDTO dto){
        Book entity = modelMapper.map( dto, Book.class);
        entity = service.save(entity);
        return modelMapper.map( entity, BookDTO.class);
    }
    @GetMapping("{id}")
    @Operation(operationId = "getBook", summary = "Get a book",
            description = "Retrieve a catalog record by ID.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public BookDTO get(@PathVariable Long id){
        return service
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteBook", summary = "Delete a book",
            description = "Only books without inventory can be deleted.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public void delete(@PathVariable Long id){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @Operation(operationId = "updateBook", summary = "Update a book",
            description = "Send validated JSON containing title, author, and ISBN. Only title and author are updated; ISBN remains unchanged.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO dto){
        return service.getById(id).map(book ->{
            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            book = service.update(book);
            return modelMapper.map(book, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @GetMapping
    @Operation(operationId = "findBooks", summary = "Search the catalog",
            description = "Optional filters combine with AND. String filters use case-insensitive partial matching.")
    @Parameters({
            @Parameter(name = "id", in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int64")),
            @Parameter(name = "title", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "author", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "isbn", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public Page<BookDTO> find(@Parameter(hidden = true) BookDTO dto, @ParameterObject Pageable pageRequest){
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDTO> list = result.getContent().stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @Operation(operationId = "getBookLoans", summary = "Get a book's loan history",
            description = "Includes active and returned loans for all copies of this book.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public Page<LoanDTO> loansByBook(@PathVariable Long id, @ParameterObject Pageable pageable){
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> list = result.getContent().stream().map(LoanDTO::from).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, result.getTotalElements());

    }

}
