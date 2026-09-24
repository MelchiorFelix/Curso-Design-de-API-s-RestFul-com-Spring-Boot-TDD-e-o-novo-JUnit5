package com.melchiorfelix.libraryapi.api.dto;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberStatusRequest(
        @Schema(example = "false", description = "False suspends borrowing; true reactivates the member")
        @NotNull(message = "Active status is required") Boolean active) {
}
