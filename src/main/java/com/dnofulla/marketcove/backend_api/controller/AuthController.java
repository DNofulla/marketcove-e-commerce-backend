package com.dnofulla.marketcove.backend_api.controller;

import com.dnofulla.marketcove.backend_api.dto.auth.*;
import com.dnofulla.marketcove.backend_api.service.AuthenticationService;
import com.dnofulla.marketcove.backend_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registration request received for email: {}", request.getEmail());
            AuthenticationResponse response = authenticationService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Registration failed for email: {}, Error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request received for email: {}", request.getEmail());
            AuthenticationResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login failed for email: {}, Error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed. Please try again."));
        }
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing or invalid refresh token"));
            }

            String refreshToken = authHeader.substring(7);
            AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed. Please try again."));
        }
    }

    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            log.info("Password reset request for email: {}", request.getEmail());
            String resetToken = userService.generatePasswordResetToken(request.getEmail());

            // TODO: Send email with reset token
            // For now, we'll return the token in the response (remove this in production)
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset instructions have been sent to your email",
                    "resetToken", resetToken // Remove this in production
            ));
        } catch (RuntimeException e) {
            log.error("Password reset failed for email: {}, Error: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during password reset for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Password reset failed. Please try again."));
        }
    }

    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        try {
            log.info("Password reset confirmation request");

            if (!request.isPasswordMatching()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password and confirm password do not match"));
            }

            boolean success = userService.resetPassword(request.getToken(), request.getNewPassword());

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or expired reset token"));
            }
        } catch (Exception e) {
            log.error("Unexpected error during password reset confirmation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Password reset failed. Please try again."));
        }
    }

    /**
     * Verify email
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            log.info("Email verification request with token: {}", token);
            boolean success = userService.verifyEmail(token);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or expired verification token"));
            }
        } catch (Exception e) {
            log.error("Unexpected error during email verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Email verification failed. Please try again."));
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            log.error("Error checking email existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check email availability"));
        }
    }

    /**
     * Get user statistics (for admin dashboard)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            var stats = userService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving user statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user statistics"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "MarketCove Authentication Service",
                "timestamp", System.currentTimeMillis()));
    }
}