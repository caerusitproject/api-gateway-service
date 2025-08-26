package com.caerus.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/users/**")
                                .filters(f -> f
                                        //.stripPrefix(1)
//                                .requestRateLimiter(c -> {
//                                    c.setRateLimiter(redisRateLimiterSpec -> redisRateLimiterSpec
//                                            .setReplenishRate(10)
//                                            .setBurstCapacity(20));
//                                    c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
//                                })
                                )
                                //.uri("lb://USER-SERVICE")
                                .uri("http://localhost:8081")
                )
                .build();
    }
}