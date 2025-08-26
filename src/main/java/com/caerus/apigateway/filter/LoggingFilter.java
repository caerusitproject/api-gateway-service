package com.caerus.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(2)
public class LoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");

        Instant start = Instant.now();

        log.info("event=request_in, method={}, path={}, correlationId={}",
                request.getMethod(), request.getURI().getPath(), correlationId);

        return chain.filter(exchange)
                .doOnSuccess(done -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long timeTaken = Instant.now().toEpochMilli() - start.toEpochMilli();

                    log.info("event=response_out, status={}, correlationId={}, timeTaken={}ms",
                            response.getStatusCode(), correlationId, timeTaken);
                })
                .doOnError(error -> {
                    long timeTaken = Instant.now().toEpochMilli() - start.toEpochMilli();

                    log.error("event=response_error, correlationId={}, timeTaken={}ms, error={}",
                            correlationId, timeTaken, error.getMessage(), error);
                });
    }
}
