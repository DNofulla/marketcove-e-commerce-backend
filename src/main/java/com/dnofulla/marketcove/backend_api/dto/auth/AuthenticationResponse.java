package com.dnofulla.marketcove.backend_api.dto.auth;

import com.dnofulla.marketcove.backend_api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for authentication response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Long refreshExpiresIn;

    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private UserRole role;
        private boolean isEmailVerified;
        private boolean isAccountLocked;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;

        // Profile-specific information
        private Long profileId;
        private String profileName; // Business name or Shop name
        private boolean isProfileVerified;
    }
}