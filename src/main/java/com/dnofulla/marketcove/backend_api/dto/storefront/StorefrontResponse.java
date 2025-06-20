package com.dnofulla.marketcove.backend_api.dto.storefront;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for storefront response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorefrontResponse {

    private Long id;
    private String storeName;
    private String storeDescription;
    private String storeUrlSlug;
    private String storeLogoUrl;
    private String storeBannerUrl;
    private String contactEmail;
    private String contactPhone;
    private String returnPolicy;
    private String shippingPolicy;
    private boolean isActive;
    private boolean isFeatured;

    // Statistics
    private Double averageRating;
    private Integer totalReviews;
    private Double totalSales;
    private Integer totalOrders;
    private Integer totalItems;

    // Social media
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String websiteUrl;

    // Owner information
    private String ownerName;
    private String ownerType; // BUSINESS or SELLER

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}