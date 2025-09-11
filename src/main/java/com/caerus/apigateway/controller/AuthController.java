package com.caerus.apigateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/gateway/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WebClient.Builder webClientBuiler;
    private static final String IDENTITY_SERVICE = "lb://identity-service";

    @PostMapping("/register")
    public Mono<ResponseEntity<Map>> register(@RequestBody Map<String, Object> body) {
        return forwardToIdentityService("/auth/register", body);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map>> login(@RequestBody Map<String, Object> body) {
        return forwardToIdentityService("/auth/login", body);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map>> refresh(@RequestBody Map<String, Object> body) {
        return forwardToIdentityService("/auth/refresh", body);

    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map>> logout(@RequestBody Map<String, Object> body) {
        return forwardToIdentityService("/auth/logout", body);

    }

    private Mono<ResponseEntity<Map>> forwardToIdentityService(String path, Object body) {
        return webClientBuiler.build()
                .post()
                .uri(IDENTITY_SERVICE + path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(ResponseEntity::ok);
    }
}
