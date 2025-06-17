package com.dnofulla.marketcove.backend_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Seller profile entity for individual sellers in the MarketCove platform
 */
@Entity
@Table(name = "seller_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Size(max = 500, message = "Shop description cannot exceed 500 characters")
    @Column(name = "shop_description", length = 500)
    private String shopDescription;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Size(max = 20, message = "Tax ID cannot exceed 20 characters")
    @Column(name = "tax_id", length = 20)
    private String taxId;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "shop_logo_url")
    private String shopLogoUrl;

    @Column(name = "shop_banner_url")
    private String shopBannerUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "identity_document_url")
    private String identityDocumentUrl;

    @Column(name = "bank_account_info", length = 100)
    private String bankAccountInfo;

    @Column(name = "commission_rate")
    private Double commissionRate = 5.0; // Default 5% commission

    @Column(name = "total_sales")
    private Double totalSales = 0.0;

    @Column(name = "total_products")
    private Integer totalProducts = 0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null)
            fullAddress.append(address);
        if (city != null)
            fullAddress.append(", ").append(city);
        if (state != null)
            fullAddress.append(", ").append(state);
        if (postalCode != null)
            fullAddress.append(" ").append(postalCode);
        if (country != null)
            fullAddress.append(", ").append(country);
        return fullAddress.toString();
    }

    public void updateRating(double newRating) {
        if (this.totalReviews == 0) {
            this.averageRating = newRating;
        } else {
            this.averageRating = ((this.averageRating * this.totalReviews) + newRating) / (this.totalReviews + 1);
        }
        this.totalReviews++;
    }
}