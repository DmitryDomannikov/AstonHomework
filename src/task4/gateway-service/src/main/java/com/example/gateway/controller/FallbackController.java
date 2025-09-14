package com.example.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/users")
    public Mono<Map<String, String>> usersFallback() {
        return Mono.just(Map.of(
                "message", "User service is temporarily unavailable",
                "status", "fallback",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
