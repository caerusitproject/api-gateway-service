package com.caerus.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleDecoding(DecodingException ex) {
        log.warn("Decoding error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "message", ex.getMessage())));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClientException(WebClientResponseException ex) {
        log.error("Webclient error: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getResponseBodyAsString());
    }
}
