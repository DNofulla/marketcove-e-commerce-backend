package com.dnofulla.marketcove.backend_api.dto.auth;

import com.dnofulla.marketcove.backend_api.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private String phoneNumber;

    @NotNull(message = "User role is required")
    private UserRole role;

    // Additional fields for Business Owner registration
    private String businessName;
    private String businessDescription;
    private String businessEmail;
    private String businessPhone;
    private String businessRegistrationNumber;
    private String taxId;
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessPostalCode;
    private String businessCountry;
    private String websiteUrl;

    // Additional fields for Seller registration
    private String shopName;
    private String shopDescription;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String bankAccountInfo;

    /**
     * Validate password confirmation
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Check if business profile fields are provided (for business owners)
     */
    public boolean hasBusinessInfo() {
        return businessName != null && !businessName.trim().isEmpty();
    }

    /**
     * Check if seller profile fields are provided (for sellers)
     */
    public boolean hasSellerInfo() {
        return shopName != null && !shopName.trim().isEmpty();
    }
}