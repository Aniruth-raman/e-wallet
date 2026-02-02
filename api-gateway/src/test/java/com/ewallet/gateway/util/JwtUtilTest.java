package com.ewallet.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret = "e-wallet-super-secret-key-for-jwt-authentication-2024-minimum-256-bits";
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        // Create a valid token
        validToken = createToken("testuser", "user-123", "USER", 3600000); // 1 hour

        // Create an expired token
        expiredToken = createToken("expireduser", "user-456", "USER", -1000); // Expired
    }

    private String createToken(String username, String userId, String role, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    @Test
    void testValidateToken_ValidToken() {
        assertTrue(jwtUtil.validateToken(validToken));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void testValidateToken_NullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateToken_EmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void testExtractUsername() {
        String username = jwtUtil.extractUsername(validToken);
        assertEquals("testuser", username);
    }

    @Test
    void testExtractUserId() {
        String userId = jwtUtil.extractUserId(validToken);
        assertEquals("user-123", userId);
    }

    @Test
    void testExtractRole() {
        String role = jwtUtil.extractRole(validToken);
        assertEquals("USER", role);
    }

    @Test
    void testExtractExpiration() {
        Date expiration = jwtUtil.extractExpiration(validToken);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        assertFalse(jwtUtil.isTokenExpired(validToken));
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        assertTrue(jwtUtil.isTokenExpired(expiredToken));
    }

    @Test
    void testExtractAllClaims() {
        Claims claims = jwtUtil.extractAllClaims(validToken);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals("user-123", claims.get("userId"));
        assertEquals("USER", claims.get("role"));
    }
}
