package com.dnofulla.marketcove.backend_api.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a new item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 200, message = "Item name must be between 2 and 200 characters")
    private String itemName;

    @Size(max = 2000, message = "Item description cannot exceed 2000 characters")
    private String itemDescription;

    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Compare at price must be greater than or equal to 0")
    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;

    private BigDecimal weight;

    @Size(max = 50, message = "Weight unit cannot exceed 50 characters")
    private String weightUnit = "kg";

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    private String tags;

    private Boolean requiresShipping = true;
    private Boolean isDigital = false;

    // SEO fields
    @Size(max = 200, message = "SEO title cannot exceed 200 characters")
    private String seoTitle;

    @Size(max = 300, message = "SEO description cannot exceed 300 characters")
    private String seoDescription;

    private List<String> imageUrls;
}