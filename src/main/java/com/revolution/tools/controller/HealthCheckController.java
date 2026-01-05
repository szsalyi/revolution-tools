package com.revolution.tools.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Simple health check controller for monitoring service availability.
 */
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse(
                "UP",
                "Revolution Tools Service is running",
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    public static class HealthResponse {
        private String status;
        private String message;
        private LocalDateTime timestamp;
    }
}
