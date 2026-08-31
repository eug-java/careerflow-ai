package com.careerflow.gateway.ratelimit;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class GatewayRateLimitFilter implements GlobalFilter, Ordered {

    private final GatewayRateLimiter rateLimiter;

    public GatewayRateLimitFilter(GatewayRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!HttpMethod.POST.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().value();
        RateLimitRule rule = resolveRule(path);
        if (rule == null) {
            return chain.filter(exchange);
        }

        String clientKey = resolveClientKey(exchange);
        if (rateLimiter.tryConsume(rule.prefix() + ":" + clientKey, rule.maxAttempts(), rule.windowSeconds())) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    private RateLimitRule resolveRule(String path) {
        if (path.startsWith("/api/v1/auth/login")) {
            return new RateLimitRule("auth-login", 20, 60);
        }
        if (path.startsWith("/api/v1/auth/register")) {
            return new RateLimitRule("auth-register", 10, 60);
        }
        if (path.startsWith("/api/v1/generations/")) {
            return new RateLimitRule("ai-generations", 30, 60);
        }
        if (path.startsWith("/api/v1/email/")) {
            return new RateLimitRule("email-api", 20, 60);
        }
        return null;
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private record RateLimitRule(String prefix, int maxAttempts, long windowSeconds) {
    }
}
