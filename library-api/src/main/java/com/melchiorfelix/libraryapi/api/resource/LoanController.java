package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.service.LoanService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Loans", description = "Checkout, return, renewal, and overdue reporting")
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "checkout", summary = "Check out a copy",
            description = "Requires a registered active member and available inventory. Omit copyId to allocate an available copy. Returns the numeric loan ID. Overdue members and members at their borrowing limit cannot check out.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public Long create(@Valid @RequestBody CheckoutRequest request) {
        return service.checkout(request).getId();
    }

    @GetMapping("{id}")
    @Operation(operationId = "getLoan", summary = "Get loan details",
            description = "Includes the member, copy barcode, due date, return date, and renewal count.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public LoanDTO get(@PathVariable Long id) {
        return LoanDTO.from(service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found")));
    }

    @PatchMapping("{id}")
    @Operation(operationId = "returnLoan", summary = "Return a loan",
            description = "Send returned=true. Repeated returns preserve the original return date and do not release copies on later loans. Reopening a loan is not supported.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public void returnBook(@PathVariable Long id, @Valid @RequestBody ReturnedLoanDTO request) {
        service.returnLoan(id);
    }

    @PostMapping("{id}/renew")
    @Operation(operationId = "renewLoan", summary = "Renew a loan",
            description = "Extends the existing due date by the configured renewal period. Returned loans, suspended or overdue members, and exhausted renewal limits are rejected.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public LoanDTO renew(@PathVariable Long id) {
        return LoanDTO.from(service.renew(id));
    }

    @GetMapping
    @Operation(operationId = "findLoans", summary = "Search loans",
            description = "Filters combine with AND. Omit filters to list all loans. Customer matches the checkout name snapshot exactly, ignoring case.")
    public Page<LoanDTO> find(@ParameterObject LoanFilterDTO filter, @ParameterObject Pageable pageable) {
        return service.find(filter, pageable).map(LoanDTO::from);
    }

    @GetMapping("overdue")
    @Operation(operationId = "getOverdueLoans", summary = "List overdue loans",
            description = "Outstanding loans become overdue the day after their due date, in the configured circulation time zone.")
    public Page<LoanDTO> overdue(@ParameterObject Pageable pageable) {
        return service.find(LoanFilterDTO.builder().overdue(true).build(), pageable).map(LoanDTO::from);
    }
}
