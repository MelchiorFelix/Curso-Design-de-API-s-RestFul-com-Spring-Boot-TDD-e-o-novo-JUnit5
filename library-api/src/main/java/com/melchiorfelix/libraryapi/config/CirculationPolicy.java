package com.melchiorfelix.libraryapi.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "library.circulation")
public class CirculationPolicy {
    @Min(1)
    private int loanDays = 14;
    @Min(1)
    private int renewalDays = 14;
    @Min(0)
    private int maxRenewals = 2;
    @Min(1)
    private int maxActiveLoans = 5;
    private String timeZone = "UTC";
}
