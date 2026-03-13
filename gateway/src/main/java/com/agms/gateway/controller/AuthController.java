package com.agms.gateway.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Issues AGMS internal JWT tokens to authenticated farmers.
 * POST /auth/token  - { "username": "farmer1", "password": "password123" }
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // In-memory users for demo purposes.
    // In production, use a UserDetailsService backed by a database.
    private static final Map<String, String> USERS = Map.of(
            "farmer1", "password123",
            "admin",   "admin123"
    );

    @PostMapping("/token")
    public Mono<Map<String, String>> getToken(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        String stored = USERS.get(username);
        if (stored == null || !stored.equals(password)) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        }

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86_400_000L)) // 24 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return Mono.just(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "expiresIn", "86400",
                "username", username
        ));
    }
}
