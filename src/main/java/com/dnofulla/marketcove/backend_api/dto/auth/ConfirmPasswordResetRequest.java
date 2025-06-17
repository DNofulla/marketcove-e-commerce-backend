package com.dnofulla.marketcove.backend_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for confirming password reset with token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPasswordResetRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /**
     * Validate password confirmation
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}