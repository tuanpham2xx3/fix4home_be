package com.fix4home.fix4home.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "testSecretKeyThatIsLongEnoughForHmacSha256Algorithm";
    private final long testExpiration = 3600000; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", testExpiration);
    }

    @Test
    @DisplayName("Should generate valid JWT token from Authentication")
    void generateToken_FromAuthentication_ShouldReturnValidToken() {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should generate valid JWT token from username")
    void generateToken_FromUsername_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void getUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Should validate valid token")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testuser");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "not-a-jwt-token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject expired token")
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date expiredDate = new Date(System.currentTimeMillis() - 10000); // 10 seconds ago
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(expiredDate)
                .signWith(key)
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with wrong signature")
    void validateToken_WithWrongSignature_ShouldReturnFalse() {
        // Arrange - Create token with different secret
        String wrongSecret = "wrongSecretKeyThatIsAlsoLongEnoughForHmacSha256";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes());
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(wrongKey)
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should return correct expiration time")
    void getExpirationTime_ShouldReturnCorrectValue() {
        // Act
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Assert
        assertEquals(testExpiration / 1000, expirationTime);
    }

    @Test
    @DisplayName("Should extract claims from valid token")
    void getClaimsFromToken_WithValidToken_ShouldReturnClaims() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Should return null claims for invalid token")
    void getClaimsFromToken_WithInvalidToken_ShouldReturnNull() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(invalidToken);

        // Assert
        assertNull(claims);
    }

    @Test
    @DisplayName("Should calculate remaining time correctly for valid token")
    void getRemainingTime_WithValidToken_ShouldReturnCorrectTime() {
        // Arrange
        String token = jwtTokenProvider.generateToken("testuser");

        // Act
        Long remainingTime = jwtTokenProvider.getRemainingTime(token);

        // Assert
        assertNotNull(remainingTime);
        assertTrue(remainingTime > 0);
        // Should be close to the original expiration time (allowing for small timing differences)
        assertTrue(remainingTime <= testExpiration / 1000);
        assertTrue(remainingTime > (testExpiration / 1000) - 10); // Allow 10 seconds difference
    }

    @Test
    @DisplayName("Should return 0 remaining time for expired token")
    void getRemainingTime_WithExpiredToken_ShouldReturnZero() {
        // Arrange - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date expiredDate = new Date(System.currentTimeMillis() - 10000); // 10 seconds ago
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(expiredDate)
                .signWith(key)
                .compact();

        // Act
        Long remainingTime = jwtTokenProvider.getRemainingTime(expiredToken);

        // Assert
        assertEquals(0L, remainingTime);
    }

    @Test
    @DisplayName("Should return 0 remaining time for invalid token")
    void getRemainingTime_WithInvalidToken_ShouldReturnZero() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        Long remainingTime = jwtTokenProvider.getRemainingTime(invalidToken);

        // Assert
        assertEquals(0L, remainingTime);
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void generateToken_ForDifferentUsers_ShouldGenerateDifferentTokens() {
        // Arrange
        String user1 = "user1";
        String user2 = "user2";

        // Act
        String token1 = jwtTokenProvider.generateToken(user1);
        String token2 = jwtTokenProvider.generateToken(user2);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals(user1, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(user2, jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void generateToken_ForSameUserAtDifferentTimes_ShouldGenerateDifferentTokens() throws InterruptedException {
        // Arrange
        String username = "testuser";

        // Act
        String token1 = jwtTokenProvider.generateToken(username);
        Thread.sleep(1000); // Wait 1 second to ensure different issued time
        String token2 = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void generateToken_WithSpecialCharactersInUsername_ShouldWork() {
        // Arrange
        String usernameWithSpecialChars = "user@domain.com";

        // Act
        String token = jwtTokenProvider.generateToken(usernameWithSpecialChars);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(usernameWithSpecialChars, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("Should handle long username")
    void generateToken_WithLongUsername_ShouldWork() {
        // Arrange
        String longUsername = "a".repeat(255); // Very long username

        // Act
        String token = jwtTokenProvider.generateToken(longUsername);

        // Assert
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(longUsername, jwtTokenProvider.getUsernameFromToken(token));
    }
}
