package com.dnofulla.marketcove.backend_api.controller;

import com.dnofulla.marketcove.backend_api.dto.auth.*;
import com.dnofulla.marketcove.backend_api.service.AuthenticationService;
import com.dnofulla.marketcove.backend_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "User authentication and account management endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with email, password, and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists", content = @Content(examples = @ExampleObject(value = "{\"error\": \"Email already registered\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(examples = @ExampleObject(value = "{\"error\": \"Invalid email or password\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Refresh token expired or invalid"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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