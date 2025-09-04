package com.example.Task.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        Map<String,Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(com.example.Task.exception.TaskNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(Exception ex, HttpServletRequest request) {
        Map<String,Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("path", request.getRequestURI());
        return ResponseEntity.status(404).body(error);
    }
}
