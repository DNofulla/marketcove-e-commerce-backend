package com.dnofulla.marketcove.backend_api.enums;

/**
 * Enum representing different user roles in the MarketCove E-Commerce platform
 */
public enum UserRole {
    CUSTOMER("Customer"),
    SELLER("Seller"),
    BUSINESS_OWNER("Business Owner"),
    ADMIN("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}