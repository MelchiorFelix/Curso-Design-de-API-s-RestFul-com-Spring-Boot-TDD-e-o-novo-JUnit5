package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.service.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Members", description = "Member registration, borrowing privileges, and history")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService members;
    private final LoanService loans;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createMember", summary = "Register a member",
            description = "Card numbers are unique and normalized to uppercase. Members start active.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public MemberDTO create(@Valid @RequestBody MemberRequest request) {
        return MemberDTO.from(members.create(request));
    }

    @GetMapping("{id}")
    @Operation(operationId = "getMember", summary = "Get a member",
            description = "Retrieve a member profile by ID.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public MemberDTO get(@PathVariable Long id) {
        return MemberDTO.from(members.get(id));
    }

    @GetMapping
    @Operation(operationId = "findMembers", summary = "Search members",
            description = "Search name, card number, or email. Optionally filter by active status.")
    public Page<MemberDTO> find(@RequestParam(defaultValue = "") String query,
                               @RequestParam(required = false) Boolean active, @ParameterObject Pageable pageable) {
        return members.find(query, active, pageable).map(MemberDTO::from);
    }

    @PutMapping("{id}")
    @Operation(operationId = "updateMember", summary = "Update a member",
            description = "Replace card number, name, and email while retaining borrowing history.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public MemberDTO update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return MemberDTO.from(members.update(id, request));
    }

    @PatchMapping("{id}")
    @Operation(operationId = "setMemberActive", summary = "Suspend or reactivate a member",
            description = "Suspended members cannot check out or renew, but can still return books.",
            responses = {
                    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
                    @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
            })
    public MemberDTO setActive(@PathVariable Long id, @Valid @RequestBody MemberStatusRequest request) {
        return MemberDTO.from(members.setActive(id, request.active()));
    }

    @GetMapping("{id}/loans")
    @Operation(operationId = "getMemberLoans", summary = "Get a member's loan history",
            description = "Includes active and returned loans, including loans made before a name change.",
            responses = {
                    @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
            })
    public Page<LoanDTO> history(@PathVariable Long id, @ParameterObject Pageable pageable) {
        members.get(id);
        return loans.getLoansByMember(id, pageable).map(LoanDTO::from);
    }
}
