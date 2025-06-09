package com.kbf.employee.controller;

import com.kbf.employee.dto.*;
import com.kbf.employee.security.JwtTokenProvider;
import com.kbf.employee.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new LoginResponse(jwt, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body(new LoginResponse("Invalid refresh token", null));
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) tokenProvider.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logoutUser(HttpServletRequest request) {
        String token = tokenProvider.getJwtFromRequest(request);

        if (token != null) {
            tokenProvider.blacklistToken(token);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new LogoutResponse("Logout successful"));
        }

        return ResponseEntity.badRequest().body(new LogoutResponse("No token provided"));
    }
}