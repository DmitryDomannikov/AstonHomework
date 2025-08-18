package com.example.userservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import java.util.Optional;

@Configuration
public class AppConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users");
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}