package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.model.entity.CopyStatus;
import com.melchiorfelix.libraryapi.service.CopyService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Copies", description = "Physical inventory and availability")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CopyController {
    private final CopyService copies;

    @PostMapping("books/{bookId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createCopy", summary = "Register a physical copy",
            description = "The barcode must be unique. New copies are available for checkout.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public CopyDTO create(@PathVariable Long bookId, @Valid @RequestBody CopyRequest request) {
        return CopyDTO.from(copies.create(bookId, request));
    }

    @GetMapping("books/{bookId}/copies")
    @Operation(operationId = "findCopies", summary = "List a book's copies",
            description = "Optionally filter by AVAILABLE, ON_LOAN, or WITHDRAWN.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public Page<CopyDTO> find(@PathVariable Long bookId,
                             @RequestParam(required = false) CopyStatus status, @ParameterObject Pageable pageable) {
        return copies.find(bookId, status, pageable).map(CopyDTO::from);
    }

    @PostMapping("copies/{id}/withdraw")
    @Operation(operationId = "withdrawCopy", summary = "Withdraw a copy",
            description = "A copy on loan cannot be withdrawn. Repeating withdrawal is safe.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public CopyDTO withdraw(@PathVariable Long id) {
        return CopyDTO.from(copies.withdraw(id));
    }
}
