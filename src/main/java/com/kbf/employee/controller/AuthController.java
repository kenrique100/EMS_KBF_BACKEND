package com.kbf.employee.controller;

import com.kbf.employee.dto.*;
import com.kbf.employee.exception.InvalidTokenException;
import com.kbf.employee.model.Employee;
import com.kbf.employee.security.JwtTokenProvider;
import com.kbf.employee.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            UserResponse userResponse = createUserResponse(userPrincipal.getEmployee());

            return ResponseEntity.ok(new LoginResponse(jwt, refreshToken, userResponse));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.unauthorized("Invalid username or password"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("User not found"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            tokenProvider.validateToken(refreshToken); // This will throw InvalidTokenException if invalid

            String username = tokenProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = tokenProvider.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            String newAccessToken = tokenProvider.generateAccessToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken));
        } catch (InvalidTokenException e) {
            throw e; // Let the GlobalExceptionHandler handle it
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound("User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.internalError("Token refresh failed"));
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        try {
            String token = tokenProvider.getJwtFromRequest(request);
            if (token == null) {
                throw new InvalidTokenException("Missing authorization token");
            }

            tokenProvider.validateToken(token); // Validate before blacklisting
            tokenProvider.blacklistToken(token);
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new LogoutResponse("Logout successful"));
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.unauthorized(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(createUserResponse(userPrincipal.getEmployee()));
    }

    private UserResponse createUserResponse(Employee employee) {
        return new UserResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                employee.getEmail(),
                employee.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
        );
    }
}