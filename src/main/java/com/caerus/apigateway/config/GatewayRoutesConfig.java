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
                .route("identity-service", r -> r.path("/auth/**")
                        .uri("lb://identity-service"))

                .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisLimiterConfig.redisRateLimiter());
                        }))
                        .uri("lb://user-service"))

                .route("ticket-service", r -> r.path("/api/v1/tickets/**", "/api/v1/files/**",
                                "/api/v1/assets/**", "/api/v1/categories/**")
                        .filters(f -> f.requestRateLimiter(config -> {
                            config.setRateLimiter(redisLimiterConfig.redisRateLimiter());
                        }))
                        .uri("lb://ticket-service"))

                .build();
    }
}