package com.nadeemr.ebanking.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT Authentication Filter for validating JWT tokens
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    private final SecretKey secretKey;
    
    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader(HEADER_NAME);
        
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            String token = authHeader.substring(TOKEN_PREFIX.length());
            
            try {
                Authentication authentication = validateTokenAndGetAuthentication(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("JWT authentication successful for user: {}", authentication.getName());
                }
            } catch (Exception e) {
                logger.warn("JWT authentication failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid or expired JWT token\"}");
                response.setContentType("application/json");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Authentication validateTokenAndGetAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String customerId = claims.getSubject();
            String customerRole = claims.get("role", String.class);
            
            if (customerId != null && customerId.matches("^P-[0-9]{10}$")) {
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + (customerRole != null ? customerRole : "CUSTOMER"))
                );
                
                return new UsernamePasswordAuthenticationToken(customerId, null, authorities);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error validating JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs") ||
               path.equals("/health") ||
               path.equals("/metrics");
    }
}
