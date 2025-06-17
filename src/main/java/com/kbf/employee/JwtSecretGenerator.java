package com.kbf.employee;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        // Generate a new secure random key for HS512 algorithm
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        // Convert to Base64 encoded string
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("New JWT Secret Key:");
        System.out.println(base64Key);
    }
}