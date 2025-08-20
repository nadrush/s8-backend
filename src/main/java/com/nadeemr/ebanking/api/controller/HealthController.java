package com.nadeemr.ebanking.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for basic application status
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check APIs")
public class HealthController {
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${server.port}")
    private String serverPort;
    
    @GetMapping
    @Operation(
        summary = "Simple health check",
        description = "Returns basic application health status"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Application is healthy",
        content = @Content(mediaType = "application/json")
    )
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("port", serverPort);
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("message", "eBanking Transactions API is running successfully");
        
        return ResponseEntity.ok(health);
    }
}
