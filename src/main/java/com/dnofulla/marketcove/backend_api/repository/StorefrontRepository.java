package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.BusinessProfile;
import com.dnofulla.marketcove.backend_api.entity.SellerProfile;
import com.dnofulla.marketcove.backend_api.entity.Storefront;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Storefront entity
 */
@Repository
public interface StorefrontRepository extends JpaRepository<Storefront, Long> {

    // Find by business profile
    List<Storefront> findByBusinessProfile(BusinessProfile businessProfile);

    List<Storefront> findByBusinessProfileAndIsActive(BusinessProfile businessProfile, boolean isActive);

    // Find by seller profile
    List<Storefront> findBySellerProfile(SellerProfile sellerProfile);

    List<Storefront> findBySellerProfileAndIsActive(SellerProfile sellerProfile, boolean isActive);

    // Find by URL slug
    Optional<Storefront> findByStoreUrlSlug(String storeUrlSlug);

    Optional<Storefront> findByStoreUrlSlugAndIsActive(String storeUrlSlug, boolean isActive);

    // Find active storefronts
    List<Storefront> findByIsActive(boolean isActive);

    Page<Storefront> findByIsActive(boolean isActive, Pageable pageable);

    // Find featured storefronts
    List<Storefront> findByIsFeaturedAndIsActive(boolean isFeatured, boolean isActive);

    Page<Storefront> findByIsFeaturedAndIsActive(boolean isFeatured, boolean isActive, Pageable pageable);

    // Search storefronts by name
    @Query("SELECT s FROM Storefront s WHERE LOWER(s.storeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND s.isActive = true")
    Page<Storefront> searchByStoreName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find storefronts by owner user ID
    @Query("SELECT s FROM Storefront s WHERE (s.businessProfile.user.id = :userId OR s.sellerProfile.user.id = :userId)")
    List<Storefront> findByOwnerUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Storefront s WHERE (s.businessProfile.user.id = :userId OR s.sellerProfile.user.id = :userId) AND s.isActive = :isActive")
    List<Storefront> findByOwnerUserIdAndIsActive(@Param("userId") Long userId, @Param("isActive") boolean isActive);

    // Check if store name exists (for validation)
    boolean existsByStoreName(String storeName);

    boolean existsByStoreUrlSlug(String storeUrlSlug);

    // Find top rated storefronts
    @Query("SELECT s FROM Storefront s WHERE s.isActive = true ORDER BY s.averageRating DESC, s.totalReviews DESC")
    Page<Storefront> findTopRatedStorefronts(Pageable pageable);

    // Find storefronts with most sales
    @Query("SELECT s FROM Storefront s WHERE s.isActive = true ORDER BY s.totalSales DESC")
    Page<Storefront> findTopSellingStorefronts(Pageable pageable);

    // Count storefronts by business profile
    long countByBusinessProfile(BusinessProfile businessProfile);

    long countByBusinessProfileAndIsActive(BusinessProfile businessProfile, boolean isActive);

    // Count storefronts by seller profile
    long countBySellerProfile(SellerProfile sellerProfile);

    long countBySellerProfileAndIsActive(SellerProfile sellerProfile, boolean isActive);
}