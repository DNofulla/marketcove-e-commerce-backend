package com.dnofulla.marketcove.backend_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Business profile entity for business owners in the MarketCove platform
 */
@Entity
@Table(name = "business_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Size(max = 500, message = "Business description cannot exceed 500 characters")
    @Column(name = "business_description", length = 500)
    private String businessDescription;

    @Column(name = "business_email")
    private String businessEmail;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    @Size(max = 50, message = "Business registration number cannot exceed 50 characters")
    @Column(name = "business_registration_number", length = 50)
    private String businessRegistrationNumber;

    @Size(max = 20, message = "Tax ID cannot exceed 20 characters")
    @Column(name = "tax_id", length = 20)
    private String taxId;

    @Column(name = "business_address", length = 200)
    private String businessAddress;

    @Column(name = "business_city", length = 50)
    private String businessCity;

    @Column(name = "business_state", length = 50)
    private String businessState;

    @Column(name = "business_postal_code", length = 20)
    private String businessPostalCode;

    @Column(name = "business_country", length = 50)
    private String businessCountry;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "business_logo_url")
    private String businessLogoUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "business_license_url")
    private String businessLicenseUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getFullBusinessAddress() {
        StringBuilder address = new StringBuilder();
        if (businessAddress != null)
            address.append(businessAddress);
        if (businessCity != null)
            address.append(", ").append(businessCity);
        if (businessState != null)
            address.append(", ").append(businessState);
        if (businessPostalCode != null)
            address.append(" ").append(businessPostalCode);
        if (businessCountry != null)
            address.append(", ").append(businessCountry);
        return address.toString();
    }
}