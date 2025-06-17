package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.SellerProfile;
import com.dnofulla.marketcove.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SellerProfile entity operations
 */
@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    /**
     * Find seller profile by user
     */
    Optional<SellerProfile> findByUser(User user);

    /**
     * Find seller profile by user ID
     */
    Optional<SellerProfile> findByUserId(Long userId);

    /**
     * Find seller profile by shop name (case-insensitive)
     */
    Optional<SellerProfile> findByShopNameIgnoreCase(String shopName);

    /**
     * Check if shop name exists
     */
    boolean existsByShopNameIgnoreCase(String shopName);

    /**
     * Find verified seller profiles
     */
    List<SellerProfile> findByIsVerifiedTrue();

    /**
     * Find unverified seller profiles
     */
    List<SellerProfile> findByIsVerifiedFalse();

    /**
     * Find active seller profiles
     */
    List<SellerProfile> findByIsActiveTrue();

    /**
     * Find inactive seller profiles
     */
    List<SellerProfile> findByIsActiveFalse();

    /**
     * Find verified and active seller profiles
     */
    List<SellerProfile> findByIsVerifiedTrueAndIsActiveTrue();

    /**
     * Find seller profiles by city
     */
    List<SellerProfile> findByCityIgnoreCase(String city);

    /**
     * Find seller profiles by state
     */
    List<SellerProfile> findByStateIgnoreCase(String state);

    /**
     * Find seller profiles by country
     */
    List<SellerProfile> findByCountryIgnoreCase(String country);

    /**
     * Find top-rated sellers
     */
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.isVerified = true AND sp.isActive = true " +
            "ORDER BY sp.averageRating DESC, sp.totalReviews DESC")
    List<SellerProfile> findTopRatedSellers();

    /**
     * Find sellers with rating above threshold
     */
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.averageRating >= :minRating AND sp.totalReviews >= :minReviews")
    List<SellerProfile> findSellersWithRatingAbove(@Param("minRating") double minRating,
            @Param("minReviews") int minReviews);

    /**
     * Find best performing sellers by total sales
     */
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.isVerified = true AND sp.isActive = true " +
            "ORDER BY sp.totalSales DESC")
    List<SellerProfile> findBestPerformingSellers();

    /**
     * Count verified sellers
     */
    long countByIsVerifiedTrue();

    /**
     * Count active sellers
     */
    long countByIsActiveTrue();

    /**
     * Search seller profiles by shop name or description
     */
    @Query("SELECT sp FROM SellerProfile sp WHERE " +
            "LOWER(sp.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sp.shopDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SellerProfile> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find sellers by commission rate
     */
    List<SellerProfile> findByCommissionRate(Double commissionRate);

    /**
     * Find sellers with commission rate below threshold
     */
    @Query("SELECT sp FROM SellerProfile sp WHERE sp.commissionRate <= :maxRate")
    List<SellerProfile> findSellersWithCommissionBelow(@Param("maxRate") double maxRate);
}