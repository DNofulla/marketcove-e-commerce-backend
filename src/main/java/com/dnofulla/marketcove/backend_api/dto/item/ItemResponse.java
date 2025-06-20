package com.dnofulla.marketcove.backend_api.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for item response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String itemName;
    private String itemDescription;
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private BigDecimal weight;
    private String weightUnit;
    private String category;
    private String tags;
    private boolean isActive;
    private boolean isFeatured;
    private boolean requiresShipping;
    private boolean isDigital;

    // SEO fields
    private String seoTitle;
    private String seoDescription;

    // Statistics
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalSales;
    private Integer viewCount;

    // Images
    private List<String> imageUrls;
    private String primaryImageUrl;

    // Storefront information
    private Long storefrontId;
    private String storefrontName;

    // Computed fields
    private boolean onSale;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private boolean lowStock;
    private boolean outOfStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}