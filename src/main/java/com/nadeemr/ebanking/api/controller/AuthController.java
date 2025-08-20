package com.nadeemr.ebanking.api.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Authentication controller for generating JWT tokens
 * Note: This is a simplified implementation for testing purposes.
 * In production, this should include proper user authentication with credentials.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "JWT token generation for testing")
public class AuthController {
    
    private final SecretKey secretKey;
    
    public AuthController(@Value("${app.jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @PostMapping("/token")
    @Operation(
        summary = "Generate JWT token for testing",
        description = "Generates a JWT token for the specified customer ID. This is a simplified endpoint for testing purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "JWT token generated successfully",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid customer ID format",
            content = @Content()
        )
    })
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody TokenRequest request) {
        
        // Create JWT token
        String token = Jwts.builder()
                .setSubject(request.customerId)
                .claim("role", "CUSTOMER")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS))) // 24 hours expiration
                .signWith(secretKey)
                .compact();
        
        return ResponseEntity.ok(new TokenResponse(token, "Bearer", 86400)); // 24 hours in seconds
    }
    
    @GetMapping("/sample-tokens")
    @Operation(
        summary = "Get sample JWT tokens",
        description = "Returns pre-generated JWT tokens for the customer IDs that have sample data"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Sample tokens retrieved successfully",
        content = @Content(schema = @Schema(implementation = SampleTokensResponse.class))
    )
    public ResponseEntity<SampleTokensResponse> getSampleTokens() {
        
        // Generate tokens for the sample customers in our data.sql
        String token1 = generateTokenForCustomer("P-0123456789");
        String token2 = generateTokenForCustomer("P-9876543210");
        
        return ResponseEntity.ok(new SampleTokensResponse(token1, token2));
    }
    
    private String generateTokenForCustomer(String customerId) {
        return Jwts.builder()
                .setSubject(customerId)
                .claim("role", "CUSTOMER")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .signWith(secretKey)
                .compact();
    }
    
    // DTOs
    public static class TokenRequest {
        @NotBlank(message = "Customer ID is required")
        @Pattern(regexp = "^P-[0-9]{10}$", message = "Customer ID must be in format P-XXXXXXXXXX")
        public String customerId;
        
        public TokenRequest() {}
        
        public TokenRequest(String customerId) {
            this.customerId = customerId;
        }
    }
    
    public static class TokenResponse {
        public String accessToken;
        public String tokenType;
        public long expiresIn;
        
        public TokenResponse() {}
        
        public TokenResponse(String accessToken, String tokenType, long expiresIn) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
        }
    }
    
    public static class SampleTokensResponse {
        public String customerP0123456789Token;
        public String customerP9876543210Token;
        public String usage;
        
        public SampleTokensResponse() {}
        
        public SampleTokensResponse(String token1, String token2) {
            this.customerP0123456789Token = token1;
            this.customerP9876543210Token = token2;
            this.usage = "Use these tokens in the Authorization header as 'Bearer <token>'";
        }
    }
}
