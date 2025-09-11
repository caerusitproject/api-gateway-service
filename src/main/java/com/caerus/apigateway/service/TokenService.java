package com.caerus.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final ReactiveJwtDecoder reactiveJwtDecoder;

    public Mono<Jwt> validateToken(String token){
        try{
            return reactiveJwtDecoder.decode(token);
        } catch (Exception e){
            return Mono.error(e);
        }
    }
}
