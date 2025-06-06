package com.kbf.employee.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfigProperties {
    @NotBlank(message = "JWT secret key must not be blank")
    private String secret;

    @Positive(message = "Access token expiration must be positive")
    private long accessTokenExpirationMs;

    @Positive(message = "Refresh token expiration must be positive")
    private long refreshTokenExpirationMs;
}