package com.dnofulla.marketcove.backend_api.dto.storefront;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new storefront
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStorefrontRequest {

    @NotBlank(message = "Store name is required")
    @Size(min = 2, max = 100, message = "Store name must be between 2 and 100 characters")
    private String storeName;

    @Size(max = 1000, message = "Store description cannot exceed 1000 characters")
    private String storeDescription;

    @Size(max = 100, message = "Store URL slug cannot exceed 100 characters")
    private String storeUrlSlug;

    @Email(message = "Please provide a valid contact email")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    @Size(max = 2000, message = "Return policy cannot exceed 2000 characters")
    private String returnPolicy;

    @Size(max = 2000, message = "Shipping policy cannot exceed 2000 characters")
    private String shippingPolicy;

    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String websiteUrl;
}