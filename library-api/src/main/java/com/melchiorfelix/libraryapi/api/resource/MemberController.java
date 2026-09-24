package com.melchiorfelix.libraryapi.api.resource;

import com.melchiorfelix.libraryapi.api.dto.*;
import com.melchiorfelix.libraryapi.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService members;
    private final LoanService loans;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDTO create(@Valid @RequestBody MemberRequest request) {
        return MemberDTO.from(members.create(request));
    }

    @GetMapping("{id}")
    public MemberDTO get(@PathVariable Long id) {
        return MemberDTO.from(members.get(id));
    }

    @GetMapping
    public Page<MemberDTO> find(@RequestParam(defaultValue = "") String query,
                               @RequestParam(required = false) Boolean active, Pageable pageable) {
        return members.find(query, active, pageable).map(MemberDTO::from);
    }

    @PutMapping("{id}")
    public MemberDTO update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return MemberDTO.from(members.update(id, request));
    }

    @PatchMapping("{id}")
    public MemberDTO setActive(@PathVariable Long id, @Valid @RequestBody MemberStatusRequest request) {
        return MemberDTO.from(members.setActive(id, request.active()));
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> history(@PathVariable Long id, Pageable pageable) {
        members.get(id);
        return loans.getLoansByMember(id, pageable).map(LoanDTO::from);
    }
}
