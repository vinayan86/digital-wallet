package com.vinay.digital_wallet.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Import Decoders
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class); // Add Logger

    @Value("${jwt.secret}")
    private String secret;

    // Optional: Make expiration time configurable
    @Value("${jwt.expiration.ms:86400000}") // Default to 1 day (86,400,000 milliseconds)
    private long expirationTimeMs;

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                // Use the configurable expiration time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log the exception for debugging purposes
            logger.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        // Corrected: Assume 'secret' from application.properties is Base64 encoded
        // and decode it before creating the key.
        // This aligns with the common practice for secrets and the comment itself.
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            // Recommended: Ensure the key is of sufficient length for HS256 (at least 32 bytes/256 bits)
            if (keyBytes.length < 32) {
                logger.error("JWT secret key is too short for HS256. It should be at least 32 bytes (256 bits) after Base64 decoding.");
                // Potentially throw an exception or handle this more robustly in a real application
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode JWT secret from Base64. Please ensure 'jwt.secret' in your configuration is a valid Base64 string.", e);
            throw new IllegalStateException("Invalid JWT secret configuration.", e);
        }
    }
}