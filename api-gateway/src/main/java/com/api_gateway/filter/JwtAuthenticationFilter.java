package com.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            log.debug("Processing request to path: {}", path);

            // Skip auth for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint accessed: {}", path);
                return chain.filter(exchange);
            }

            // Extract JWT from Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate and parse JWT
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtSecret.getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Extract user information
                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();
                String roles = claims.get("roles", String.class);

                log.debug("JWT validated for user: {} with roles: {}", userId, roles);

                // Forward user info to downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (ExpiredJwtException e) {
                log.error("JWT token expired for path: {}", path);
                return onError(exchange, "JWT token has expired", HttpStatus.UNAUTHORIZED);
            } catch (JwtException e) {
                log.error("Invalid JWT token for path: {}", path, e);
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("Error processing JWT for path: {}", path, e);
                return onError(exchange, "Error processing authentication", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return
                // Auth service public endpoints
                path.startsWith("/auth/login") ||
                        path.startsWith("/auth/register") ||

                        // OAuth2 endpoints
                        path.startsWith("/oauth2/") ||
                        path.startsWith("/login/oauth2/") ||
                        path.equals("/login.html") ||

                        // Restaurant public endpoints
                        path.startsWith("/api/restaurants/public") ||

                        // Health check endpoints
                        path.startsWith("/actuator/health") ||

                        // Fallback endpoint
                        path.equals("/fallback");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                err, httpStatus.value(), Instant.now());

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configuration properties if needed
    }
}