package com.fix4home.fix4home.security;

import com.fix4home.fix4home.model.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return generateToken(customUserDetails.getUser());
        }

        if (principal instanceof UserDetails userDetails) {
            return buildToken(userDetails.getUsername(), buildUsernameOnlyClaims(userDetails.getUsername()));
        }

        return buildToken(authentication.getName(), buildUsernameOnlyClaims(authentication.getName()));
    }

    public String generateToken(String username) {
        return buildToken(username, buildUsernameOnlyClaims(username));
    }

    public String generateToken(User user) {
        return buildToken(user.getUsername(), buildUserClaims(user));
    }

    private String buildToken(String subject, Map<String, Object> claims) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

        JwtBuilder builder = Jwts.builder();

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private Map<String, Object> buildUserClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        return claims;
    }

    private Map<String, Object> buildUsernameOnlyClaims(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return claims;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public long getExpirationTime() {
        return jwtExpirationInMs / 1000; // Convert to seconds
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error getting claims from token", e);
            return null;
        }
    }

    public Long getRemainingTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) return 0L;

            Date expiration = claims.getExpiration();
            Date now = new Date();

            long diff = expiration.getTime() - now.getTime();
            return diff > 0 ? diff / 1000 : 0; // Convert to seconds
        } catch (Exception e) {
            return 0L;
        }
    }
}