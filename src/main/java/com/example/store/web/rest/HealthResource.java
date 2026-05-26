package com.example.store.web.rest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthResource {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("REST request to check application health");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "store",
                "checkedAt", Instant.now().toString()));
    }
}
