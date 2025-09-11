package com.caerus.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    private final RedisLimiterConfig redisLimiterConfig;

    public GatewayRoutesConfig(RedisLimiterConfig redisLimiterConfig) {
        this.redisLimiterConfig = redisLimiterConfig;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity_service", r -> r.path("/auth/**", "/gateway/auth/**")
                        .uri("lb://identity-service"))

                .route("user_service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisLimiterConfig.redisRateLimiter());
                        }))
                        .uri("lb://user-service"))

                .route("ticket_service", r -> r.path("/api/v1/tickets/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisLimiterConfig.redisRateLimiter());
                        }))
                        .uri("lb://ticket-service"))

                .build();
    }
}