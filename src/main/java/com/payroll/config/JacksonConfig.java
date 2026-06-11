package com.payroll.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Boot 4 no longer auto-exposes an {@link ObjectMapper} bean unless the
 * dedicated Jackson starter is present. The import/export pipeline uses it to
 * (de)serialise staged rows, column mappings, and row errors — all plain
 * strings and numbers, so a default mapper is sufficient.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
