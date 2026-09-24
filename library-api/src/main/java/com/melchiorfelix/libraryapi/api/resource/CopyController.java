package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.model.entity.CopyStatus;
import com.melchiorfelix.libraryapi.service.CopyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CopyController {
    private final CopyService copies;

    @PostMapping("books/{bookId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public CopyDTO create(@PathVariable Long bookId, @Valid @RequestBody CopyRequest request) {
        return CopyDTO.from(copies.create(bookId, request));
    }

    @GetMapping("books/{bookId}/copies")
    public Page<CopyDTO> find(@PathVariable Long bookId,
                             @RequestParam(required = false) CopyStatus status, Pageable pageable) {
        return copies.find(bookId, status, pageable).map(CopyDTO::from);
    }

    @PostMapping("copies/{id}/withdraw")
    public CopyDTO withdraw(@PathVariable Long id) {
        return CopyDTO.from(copies.withdraw(id));
    }
}
