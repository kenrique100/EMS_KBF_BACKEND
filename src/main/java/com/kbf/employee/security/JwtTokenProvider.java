package com.kbf.employee.security;

import com.kbf.employee.config.JwtConfigProperties;
import com.kbf.employee.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtTokenProvider(JwtConfigProperties jwtConfig) {
        if (!StringUtils.hasText(jwtConfig.getSecret())) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
            this.key = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT Key Algorithm: {}, Key length: {} bits",
                    this.key.getAlgorithm(), keyBytes.length * 8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JWT secret key format", e);
        }

        this.accessTokenExpirationMs = jwtConfig.getAccessTokenExpirationMs();
        this.refreshTokenExpirationMs = jwtConfig.getRefreshTokenExpirationMs();
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            if (isTokenBlacklisted(token)) {
                log.error("Token is blacklisted");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(30)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("Expired JWT token", ex);
        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException("Unsupported JWT token", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        } catch (SignatureException ex) {
            throw new JwtAuthenticationException("Invalid JWT signature", ex);
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException("JWT claims string is empty", ex);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}