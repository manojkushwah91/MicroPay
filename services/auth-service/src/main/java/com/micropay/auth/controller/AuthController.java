package com.micropay.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micropay.auth.dto.AuthResponse;
import com.micropay.auth.dto.LoginRequest;
import com.micropay.auth.dto.RegisterRequest;
import com.micropay.auth.dto.ForgotPasswordRequest;
import com.micropay.auth.dto.ResetPasswordRequest;
import com.micropay.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
// @CrossOrigin(origins = "*", maxAge = 3600) <--- IMPORTANT: Commented out to prevent Double CORS Error
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.substring(7));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("Auth service is working");
    }

    @PostMapping("/test-post")
    public ResponseEntity<String> testPost() {
        logger.info("Test POST endpoint called");
        return ResponseEntity.ok("Auth service POST is working");
    }

    @PostMapping("/echo")
    public ResponseEntity<String> echo(@RequestBody String body) {
        logger.info("Echo endpoint called with body: {}", body);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo-register")
    public ResponseEntity<String> echoRegister(@RequestBody RegisterRequest request) {
        logger.info("Echo register endpoint called with: {}", request.getEmail());
        return ResponseEntity.ok("Received: " + request.getEmail());
    }

    @PostMapping("/test-register")
    public ResponseEntity<String> testRegister() {
        try {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("test@example.com");
            logger.info("RegisterRequest test successful: {}", req.getEmail());
            return ResponseEntity.ok("RegisterRequest test successful");
        } catch (Exception e) {
            logger.error("RegisterRequest test failed", e);
            return ResponseEntity.status(500).body("RegisterRequest test failed: " + e.getMessage());
        }
    }

    @PostMapping("/test-json")
    public ResponseEntity<String> testJson(@RequestBody String json) {
        logger.info("Received JSON: {}", json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            RegisterRequest request = mapper.readValue(json, RegisterRequest.class);
            logger.info("Successfully parsed RegisterRequest: {}", request.getEmail());
            return ResponseEntity.ok("JSON parsed successfully: " + request.getEmail());
        } catch (Exception e) {
            logger.error("Failed to parse JSON", e);
            return ResponseEntity.status(500).body("Failed to parse JSON: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Received registration request for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Received login request for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}