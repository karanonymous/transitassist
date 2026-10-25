package com.transitassist.transitassist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** One shared clock. The bus simulator and request service read time from here (easy to replace in tests). */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
