package com.melchiorfelix.libraryapi.api.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanFilterDTO {
    private String isbn;
    @Schema(description = "Case-insensitive exact match against the member name at checkout")
    private String customer;
    private Long memberId;
    private Boolean returned;
    @Schema(description = "True selects outstanding loans past their due date; false selects all other loans")
    private Boolean overdue;
}
