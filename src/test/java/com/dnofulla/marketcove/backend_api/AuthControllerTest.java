package com.dnofulla.marketcove.backend_api;

import com.dnofulla.marketcove.backend_api.dto.auth.*;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.service.AuthenticationService;
import com.dnofulla.marketcove.backend_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test class for AuthController endpoints
 */
@WebMvcTest(controllers = { com.dnofulla.marketcove.backend_api.controller.AuthController.class })
@DisplayName("AuthController Tests")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationService authenticationService;

        @MockBean
        private UserService userService;

        @MockBean
        private com.dnofulla.marketcove.backend_api.util.JwtUtil jwtUtil;

        @MockBean
        private com.dnofulla.marketcove.backend_api.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        private RegisterRequest validRegisterRequest;
        private LoginRequest validLoginRequest;
        private AuthenticationResponse mockAuthResponse;
        private UserService.UserStats mockUserStats;

        @BeforeEach
        void setUp() {
                // Setup valid register request
                validRegisterRequest = new RegisterRequest();
                validRegisterRequest.setFirstName("John");
                validRegisterRequest.setLastName("Doe");
                validRegisterRequest.setEmail("john.doe@test.com");
                validRegisterRequest.setPassword("TestPassword123!");
                validRegisterRequest.setConfirmPassword("TestPassword123!");
                validRegisterRequest.setRole(UserRole.CUSTOMER);

                // Setup valid login request
                validLoginRequest = new LoginRequest();
                validLoginRequest.setEmail("john.doe@test.com");
                validLoginRequest.setPassword("TestPassword123!");

                // Setup mock auth response
                mockAuthResponse = new AuthenticationResponse();
                mockAuthResponse.setAccessToken("mock-access-token");
                mockAuthResponse.setRefreshToken("mock-refresh-token");
                mockAuthResponse.setTokenType("Bearer");
                mockAuthResponse.setExpiresIn(86400L);

                // Setup mock user stats
                mockUserStats = new UserService.UserStats(5L, 3L, 2L, 1L, 1L, 1L);
        }

        @Nested
        @DisplayName("Health Endpoint Tests")
        class HealthEndpointTests {

                @Test
                @DisplayName("Should return health status successfully")
                void testHealthEndpoint() throws Exception {
                        mockMvc.perform(get("/api/auth/health"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.status").value("UP"))
                                        .andExpect(jsonPath("$.service").value("MarketCove Authentication Service"))
                                        .andExpect(jsonPath("$.timestamp").exists());
                }
        }

        @Nested
        @DisplayName("Registration Endpoint Tests")
        class RegistrationEndpointTests {

                @Test
                @DisplayName("Should register customer successfully")
                void testRegisterCustomerSuccess() throws Exception {
                        when(authenticationService.register(any(RegisterRequest.class)))
                                        .thenReturn(mockAuthResponse);

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                                        .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                                        .andExpect(jsonPath("$.tokenType").value("Bearer"));

                        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
                }

                @Test
                @DisplayName("Should register seller successfully")
                void testRegisterSellerSuccess() throws Exception {
                        validRegisterRequest.setRole(UserRole.SELLER);
                        validRegisterRequest.setShopName("John's Electronics");
                        validRegisterRequest.setShopDescription("Quality electronics");

                        when(authenticationService.register(any(RegisterRequest.class)))
                                        .thenReturn(mockAuthResponse);

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").exists());

                        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
                }

                @Test
                @DisplayName("Should register business owner successfully")
                void testRegisterBusinessOwnerSuccess() throws Exception {
                        validRegisterRequest.setRole(UserRole.BUSINESS_OWNER);
                        validRegisterRequest.setBusinessName("John's Corp");
                        validRegisterRequest.setBusinessDescription("Software solutions");

                        when(authenticationService.register(any(RegisterRequest.class)))
                                        .thenReturn(mockAuthResponse);

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").exists());

                        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
                }

                @Test
                @DisplayName("Should return 400 when email already exists")
                void testRegisterEmailAlreadyExists() throws Exception {
                        when(authenticationService.register(any(RegisterRequest.class)))
                                        .thenThrow(
                                                        new RuntimeException("User already exists with email: "
                                                                        + validRegisterRequest.getEmail()));

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error")
                                                        .value("User already exists with email: "
                                                                        + validRegisterRequest.getEmail()));
                }

                @Test
                @DisplayName("Should return 400 when first name is missing")
                void testRegisterMissingFirstName() throws Exception {
                        validRegisterRequest.setFirstName("");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when email is invalid")
                void testRegisterInvalidEmail() throws Exception {
                        validRegisterRequest.setEmail("invalid-email");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when password is too short")
                void testRegisterPasswordTooShort() throws Exception {
                        validRegisterRequest.setPassword("123");
                        validRegisterRequest.setConfirmPassword("123");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when confirm password is missing")
                void testRegisterMissingConfirmPassword() throws Exception {
                        validRegisterRequest.setConfirmPassword("");

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 on unexpected error")
                void testRegisterUnexpectedError() throws Exception {
                        when(authenticationService.register(any(RegisterRequest.class)))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(post("/api/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Login Endpoint Tests")
        class LoginEndpointTests {

                @Test
                @DisplayName("Should login successfully with valid credentials")
                void testLoginSuccess() throws Exception {
                        when(authenticationService.login(any(LoginRequest.class)))
                                        .thenReturn(mockAuthResponse);

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                                        .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));

                        verify(authenticationService, times(1)).login(any(LoginRequest.class));
                }

                @Test
                @DisplayName("Should return 401 with invalid credentials")
                void testLoginInvalidCredentials() throws Exception {
                        when(authenticationService.login(any(LoginRequest.class)))
                                        .thenThrow(new RuntimeException("Invalid email or password"));

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error").value("Invalid email or password"));
                }

                @Test
                @DisplayName("Should return 400 when email is missing")
                void testLoginMissingEmail() throws Exception {
                        validLoginRequest.setEmail("");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when password is missing")
                void testLoginMissingPassword() throws Exception {
                        validLoginRequest.setPassword("");

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 401 on unexpected error")
                void testLoginUnexpectedError() throws Exception {
                        when(authenticationService.login(any(LoginRequest.class)))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Refresh Token Endpoint Tests")
        class RefreshTokenEndpointTests {

                @Test
                @DisplayName("Should refresh token successfully")
                void testRefreshTokenSuccess() throws Exception {
                        when(authenticationService.refreshToken(anyString()))
                                        .thenReturn(mockAuthResponse);

                        mockMvc.perform(post("/api/auth/refresh-token")
                                        .header("Authorization", "Bearer valid-refresh-token"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.accessToken").value("mock-access-token"));

                        verify(authenticationService, times(1)).refreshToken("valid-refresh-token");
                }

                @Test
                @DisplayName("Should return 400 when Authorization header is missing")
                void testRefreshTokenMissingHeader() throws Exception {
                        mockMvc.perform(post("/api/auth/refresh-token"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Missing or invalid refresh token"));
                }

                @Test
                @DisplayName("Should return 400 when Authorization header doesn't start with Bearer")
                void testRefreshTokenInvalidHeader() throws Exception {
                        mockMvc.perform(post("/api/auth/refresh-token")
                                        .header("Authorization", "Basic invalid-token"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Missing or invalid refresh token"));
                }

                @Test
                @DisplayName("Should return 401 when refresh token is invalid")
                void testRefreshTokenInvalid() throws Exception {
                        when(authenticationService.refreshToken(anyString()))
                                        .thenThrow(new RuntimeException("Invalid refresh token"));

                        mockMvc.perform(post("/api/auth/refresh-token")
                                        .header("Authorization", "Bearer invalid-token"))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error").value("Invalid refresh token"));
                }

                @Test
                @DisplayName("Should return 401 on unexpected error")
                void testRefreshTokenUnexpectedError() throws Exception {
                        when(authenticationService.refreshToken(anyString()))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(post("/api/auth/refresh-token")
                                        .header("Authorization", "Bearer valid-token"))
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.error").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Forgot Password Endpoint Tests")
        class ForgotPasswordEndpointTests {

                @Test
                @DisplayName("Should generate password reset token successfully")
                void testForgotPasswordSuccess() throws Exception {
                        PasswordResetRequest request = new PasswordResetRequest();
                        request.setEmail("test@example.com");

                        when(userService.generatePasswordResetToken(anyString()))
                                        .thenReturn("reset-token-123");

                        mockMvc.perform(post("/api/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value(
                                                        "Password reset instructions have been sent to your email"))
                                        .andExpect(jsonPath("$.resetToken").value("reset-token-123"));

                        verify(userService, times(1)).generatePasswordResetToken("test@example.com");
                }

                @Test
                @DisplayName("Should return 400 when email is invalid")
                void testForgotPasswordInvalidEmail() throws Exception {
                        PasswordResetRequest request = new PasswordResetRequest();
                        request.setEmail("invalid-email");

                        mockMvc.perform(post("/api/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when user not found")
                void testForgotPasswordUserNotFound() throws Exception {
                        PasswordResetRequest request = new PasswordResetRequest();
                        request.setEmail("notfound@example.com");

                        when(userService.generatePasswordResetToken(anyString()))
                                        .thenThrow(new RuntimeException(
                                                        "User not found with email: notfound@example.com"));

                        mockMvc.perform(post("/api/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error")
                                                        .value("User not found with email: notfound@example.com"));
                }

                @Test
                @DisplayName("Should return 400 on unexpected error")
                void testForgotPasswordUnexpectedError() throws Exception {
                        PasswordResetRequest request = new PasswordResetRequest();
                        request.setEmail("test@example.com");

                        when(userService.generatePasswordResetToken(anyString()))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(post("/api/auth/forgot-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Reset Password Endpoint Tests")
        class ResetPasswordEndpointTests {

                @Test
                @DisplayName("Should reset password successfully")
                void testResetPasswordSuccess() throws Exception {
                        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
                        request.setToken("valid-reset-token");
                        request.setNewPassword("NewPassword123!");
                        request.setConfirmPassword("NewPassword123!");

                        when(userService.resetPassword(anyString(), anyString()))
                                        .thenReturn(true);

                        mockMvc.perform(post("/api/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Password reset successfully"));

                        verify(userService, times(1)).resetPassword("valid-reset-token", "NewPassword123!");
                }

                @Test
                @DisplayName("Should return 400 when passwords don't match")
                void testResetPasswordMismatch() throws Exception {
                        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
                        request.setToken("valid-reset-token");
                        request.setNewPassword("NewPassword123!");
                        request.setConfirmPassword("DifferentPassword123!");

                        mockMvc.perform(post("/api/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error")
                                                        .value("Password and confirm password do not match"));

                        verify(userService, never()).resetPassword(anyString(), anyString());
                }

                @Test
                @DisplayName("Should return 400 when reset token is invalid")
                void testResetPasswordInvalidToken() throws Exception {
                        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
                        request.setToken("invalid-reset-token");
                        request.setNewPassword("NewPassword123!");
                        request.setConfirmPassword("NewPassword123!");

                        when(userService.resetPassword(anyString(), anyString()))
                                        .thenReturn(false);

                        mockMvc.perform(post("/api/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Invalid or expired reset token"));
                }

                @Test
                @DisplayName("Should return 500 on unexpected error")
                void testResetPasswordUnexpectedError() throws Exception {
                        ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
                        request.setToken("valid-reset-token");
                        request.setNewPassword("NewPassword123!");
                        request.setConfirmPassword("NewPassword123!");

                        when(userService.resetPassword(anyString(), anyString()))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(post("/api/auth/reset-password")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(jsonPath("$.error")
                                                        .value("Password reset failed. Please try again."));
                }
        }

        @Nested
        @DisplayName("Email Verification Endpoint Tests")
        class EmailVerificationEndpointTests {

                @Test
                @DisplayName("Should verify email successfully")
                void testVerifyEmailSuccess() throws Exception {
                        when(userService.verifyEmail(anyString()))
                                        .thenReturn(true);

                        mockMvc.perform(get("/api/auth/verify-email")
                                        .param("token", "valid-verification-token"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.message").value("Email verified successfully"));

                        verify(userService, times(1)).verifyEmail("valid-verification-token");
                }

                @Test
                @DisplayName("Should return 400 when verification token is invalid")
                void testVerifyEmailInvalidToken() throws Exception {
                        when(userService.verifyEmail(anyString()))
                                        .thenReturn(false);

                        mockMvc.perform(get("/api/auth/verify-email")
                                        .param("token", "invalid-token"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.error").value("Invalid or expired verification token"));
                }

                @Test
                @DisplayName("Should return 500 on unexpected error")
                void testVerifyEmailUnexpectedError() throws Exception {
                        when(userService.verifyEmail(anyString()))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(get("/api/auth/verify-email")
                                        .param("token", "valid-token"))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(jsonPath("$.error")
                                                        .value("Email verification failed. Please try again."));
                }
        }

        @Nested
        @DisplayName("Check Email Endpoint Tests")
        class CheckEmailEndpointTests {

                @Test
                @DisplayName("Should return true when email exists")
                void testCheckEmailExists() throws Exception {
                        when(userService.emailExists(anyString()))
                                        .thenReturn(true);

                        mockMvc.perform(get("/api/auth/check-email")
                                        .param("email", "existing@example.com"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.exists").value(true));

                        verify(userService, times(1)).emailExists("existing@example.com");
                }

                @Test
                @DisplayName("Should return false when email doesn't exist")
                void testCheckEmailNotExists() throws Exception {
                        when(userService.emailExists(anyString()))
                                        .thenReturn(false);

                        mockMvc.perform(get("/api/auth/check-email")
                                        .param("email", "nonexistent@example.com"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.exists").value(false));
                }

                @Test
                @DisplayName("Should return 500 on unexpected error")
                void testCheckEmailUnexpectedError() throws Exception {
                        when(userService.emailExists(anyString()))
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(get("/api/auth/check-email")
                                        .param("email", "test@example.com"))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(jsonPath("$.error").value("Failed to check email availability"));
                }
        }

        @Nested
        @DisplayName("User Stats Endpoint Tests")
        class UserStatsEndpointTests {

                @Test
                @DisplayName("Should return user statistics successfully")
                void testGetUserStatsSuccess() throws Exception {
                        when(userService.getUserStats())
                                        .thenReturn(mockUserStats);

                        mockMvc.perform(get("/api/auth/stats"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.totalUsers").value(5))
                                        .andExpect(jsonPath("$.verifiedUsers").value(3))
                                        .andExpect(jsonPath("$.customers").value(2))
                                        .andExpect(jsonPath("$.sellers").value(1))
                                        .andExpect(jsonPath("$.businessOwners").value(1))
                                        .andExpect(jsonPath("$.admins").value(1));

                        verify(userService, times(1)).getUserStats();
                }

                @Test
                @DisplayName("Should return 500 on unexpected error")
                void testGetUserStatsUnexpectedError() throws Exception {
                        when(userService.getUserStats())
                                        .thenThrow(new IllegalStateException("Database connection failed"));

                        mockMvc.perform(get("/api/auth/stats"))
                                        .andExpect(status().isInternalServerError())
                                        .andExpect(jsonPath("$.error").value("Failed to retrieve user statistics"));
                }
        }
}