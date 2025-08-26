package com.caerus.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        HttpStatus status = determineStatus(ex);
        String message = determineMessage(ex);

        logger.error("Error occurred - Correlation ID: {} - Status: {} - Message: {}",
                correlationId, status, message, ex);

        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        if (correlationId != null) {
            response.getHeaders().add("X-Correlation-Id", correlationId);
        }

        Map<String, Object> errorResponse = createErrorResponse(status, message, correlationId,
                exchange.getRequest().getPath().value());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            String fallbackResponse = String.format(
                    "{\"error\":\"Internal server error\",\"timestamp\":\"%s\"}",
                    Instant.now().toString()
            );

            DataBuffer buffer = response.bufferFactory().wrap(fallbackResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            return statusCode instanceof HttpStatus ? (HttpStatus) statusCode : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getReason();
        }
        return "Internal server error";
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String message, String correlationId, String path) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "correlationId", correlationId,
                "path", path
        );
    }

}
