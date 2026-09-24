package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.NotNull;

public record MemberStatusRequest(@NotNull(message = "Active status is required") Boolean active) {
}
