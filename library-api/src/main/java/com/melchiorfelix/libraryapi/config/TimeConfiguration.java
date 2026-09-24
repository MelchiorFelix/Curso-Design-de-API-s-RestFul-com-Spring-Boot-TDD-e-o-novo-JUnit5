package com.melchiorfelix.libraryapi.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfiguration {
    @Bean
    public Clock clock(CirculationPolicy policy) {
        return Clock.system(ZoneId.of(policy.getTimeZone()));
    }
}
