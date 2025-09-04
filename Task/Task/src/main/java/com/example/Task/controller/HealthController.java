package com.example.Task.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String,Object> healthCheck() {
        Map<String,Object> response = new HashMap<>();
        response.put("status","ok");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
