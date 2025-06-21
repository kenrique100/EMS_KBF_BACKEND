package com.kbf.employee.security;

import com.kbf.employee.config.JwtConfigProperties;
import com.kbf.employee.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final ConcurrentMap<String, Boolean> tokenBlacklist = new ConcurrentHashMap<>();
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(JwtConfigProperties jwtConfig, UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
        this.accessTokenExpirationMs = jwtConfig.getAccessTokenExpirationMs();
        this.refreshTokenExpirationMs = jwtConfig.getRefreshTokenExpirationMs();
    }

    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS512)
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
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlacklist.containsKey(token)) {
                log.warn("Attempt to use blacklisted token");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .setAllowedClockSkewSeconds(30)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException ex) {
            log.error("JWT token expired: {}", ex.getMessage());
            throw new JwtAuthenticationException("Token expired", ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Unsupported token", ex);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid token", ex);
        } catch (SignatureException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
            throw new JwtAuthenticationException("Signature validation failed", ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
            throw new JwtAuthenticationException("Claims string is empty", ex);
        }
    }

    public void blacklistToken(String token) {
        tokenBlacklist.put(token, true);
    }

    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }
}