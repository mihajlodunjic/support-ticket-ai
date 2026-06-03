package com.it_support_ticket_system.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecurityProperties securityProperties;

    public JwtService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String generateToken(SecurityUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(securityProperties.getTokenValiditySeconds());

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("role", user.getRole().name())
            .claim("name", user.getName())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, SecurityUser user) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        try {
            byte[] decoded = Decoders.BASE64.decode(securityProperties.getJwtSecret());
            if (decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (RuntimeException exception) {
            // Fall back to the raw secret so plain-text env values work without extra encoding.
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
