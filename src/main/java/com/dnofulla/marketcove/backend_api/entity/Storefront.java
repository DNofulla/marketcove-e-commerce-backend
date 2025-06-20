package com.dnofulla.marketcove.backend_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Storefront entity representing an online store for businesses or sellers
 */
@Entity
@Table(name = "storefronts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Storefront {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Size(max = 1000, message = "Store description cannot exceed 1000 characters")
    @Column(name = "store_description", length = 1000)
    private String storeDescription;

    @Size(max = 100, message = "Store URL slug cannot exceed 100 characters")
    @Column(name = "store_url_slug", unique = true, length = 100)
    private String storeUrlSlug;

    @Column(name = "store_logo_url")
    private String storeLogoUrl;

    @Column(name = "store_banner_url")
    private String storeBannerUrl;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "return_policy", length = 2000)
    private String returnPolicy;

    @Column(name = "shipping_policy", length = 2000)
    private String shippingPolicy;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    // Rating and review statistics
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_sales")
    private Double totalSales = 0.0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    // Social media links
    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    // Owner references - either business or seller can own a storefront
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id")
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_profile_id")
    private SellerProfile sellerProfile;

    // Items in this storefront
    @OneToMany(mappedBy = "storefront", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.storeUrlSlug == null || this.storeUrlSlug.isEmpty()) {
            this.storeUrlSlug = generateUrlSlug(this.storeName);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getOwnerName() {
        if (businessProfile != null) {
            return businessProfile.getBusinessName();
        } else if (sellerProfile != null) {
            return sellerProfile.getShopName();
        }
        return "Unknown";
    }

    public String getOwnerType() {
        if (businessProfile != null) {
            return "BUSINESS";
        } else if (sellerProfile != null) {
            return "SELLER";
        }
        return "UNKNOWN";
    }

    public User getOwnerUser() {
        if (businessProfile != null) {
            return businessProfile.getUser();
        } else if (sellerProfile != null) {
            return sellerProfile.getUser();
        }
        return null;
    }

    private String generateUrlSlug(String name) {
        if (name == null)
            return null;
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
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