package com.melchiorfelix.libraryapi.api.dto;

import com.melchiorfelix.libraryapi.model.entity.Member;

public record MemberDTO(Long id, String cardNumber, String name, String email, boolean active) {
    public static MemberDTO from(Member member) {
        return new MemberDTO(member.getId(), member.getCardNumber(), member.getName(),
                member.getEmail(), member.isActive());
    }
}
