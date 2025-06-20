package com.dnofulla.marketcove.backend_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Item entity representing products listed in storefronts
 */
@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 200, message = "Item name must be between 2 and 200 characters")
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Size(max = 2000, message = "Item description cannot exceed 2000 characters")
    @Column(name = "item_description", length = 2000)
    private String itemDescription;

    @Column(name = "sku", unique = true, length = 50)
    private String sku;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Compare at price must be greater than or equal to 0")
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 10;

    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight;

    @Size(max = 50, message = "Weight unit cannot exceed 50 characters")
    @Column(name = "weight_unit", length = 50)
    private String weightUnit = "kg";

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "requires_shipping", nullable = false)
    private boolean requiresShipping = true;

    @Column(name = "is_digital", nullable = false)
    private boolean isDigital = false;

    // SEO fields
    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Size(max = 300, message = "SEO description cannot exceed 300 characters")
    @Column(name = "seo_description", length = 300)
    private String seoDescription;

    // Rating and review statistics
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_sales")
    private Integer totalSales = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    // Image URLs - stored as JSON or comma-separated values
    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    // Primary image (first image or explicitly set)
    @Column(name = "primary_image_url")
    private String primaryImageUrl;

    // Storefront relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storefront_id", nullable = false)
    private Storefront storefront;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.sku == null || this.sku.isEmpty()) {
            this.sku = generateSku();
        }
        if (this.primaryImageUrl == null && !this.imageUrls.isEmpty()) {
            this.primaryImageUrl = this.imageUrls.get(0);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.primaryImageUrl == null && !this.imageUrls.isEmpty()) {
            this.primaryImageUrl = this.imageUrls.get(0);
        }
    }

    // Helper methods
    public boolean isOnSale() {
        return compareAtPrice != null && compareAtPrice.compareTo(price) > 0;
    }

    public BigDecimal getDiscountAmount() {
        if (isOnSale()) {
            return compareAtPrice.subtract(price);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getDiscountPercentage() {
        if (isOnSale()) {
            return getDiscountAmount()
                    .divide(compareAtPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public void updateRating(double newRating) {
        if (this.totalReviews == 0) {
            this.averageRating = newRating;
        } else {
            this.averageRating = ((this.averageRating * this.totalReviews) + newRating) / (this.totalReviews + 1);
        }
        this.totalReviews++;
    }

    public void incrementSales() {
        this.totalSales++;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void decrementStock(int quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        }
    }

    public void incrementStock(int quantity) {
        this.stockQuantity += quantity;
    }

    private String generateSku() {
        return "ITEM-" + System.currentTimeMillis();
    }
}