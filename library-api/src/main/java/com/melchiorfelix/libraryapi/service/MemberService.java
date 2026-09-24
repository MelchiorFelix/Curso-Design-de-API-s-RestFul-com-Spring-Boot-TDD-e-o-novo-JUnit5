package com.melchiorfelix.libraryapi.service;

import com.melchiorfelix.libraryapi.api.dto.MemberRequest;
import com.melchiorfelix.libraryapi.exception.BusinessException;
import com.melchiorfelix.libraryapi.model.entity.Member;
import com.melchiorfelix.libraryapi.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository repository;

    public Member create(MemberRequest request) {
        String card = normalizeCard(request.cardNumber());
        if (repository.existsByCardNumberIgnoreCase(card)) {
            throw new BusinessException("Card number already registered");
        }
        return repository.save(Member.builder().cardNumber(card).name(request.name().trim())
                .email(request.email().trim()).build());
    }

    public Member update(Long id, MemberRequest request) {
        Member member = lock(id);
        String card = normalizeCard(request.cardNumber());
        if (repository.existsByCardNumberIgnoreCaseAndIdNot(card, id)) {
            throw new BusinessException("Card number already registered");
        }
        member.setCardNumber(card);
        member.setName(request.name().trim());
        member.setEmail(request.email().trim());
        return member;
    }

    public Member setActive(Long id, boolean active) {
        Member member = lock(id);
        member.setActive(active);
        return member;
    }

    @Transactional(readOnly = true)
    public Member get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    @Transactional(readOnly = true)
    public Page<Member> find(String query, Boolean active, Pageable pageable) {
        return repository.search(query.trim(), active, pageable);
    }

    private Member lock(Long id) {
        return repository.findLockedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    private String normalizeCard(String card) {
        return card.trim().toUpperCase(Locale.ROOT);
    }
}
