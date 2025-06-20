package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.Item;
import com.dnofulla.marketcove.backend_api.entity.Storefront;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Item entity
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Find by storefront
    List<Item> findByStorefront(Storefront storefront);

    List<Item> findByStorefrontAndIsActive(Storefront storefront, boolean isActive);

    Page<Item> findByStorefrontAndIsActive(Storefront storefront, boolean isActive, Pageable pageable);

    // Find by storefront ID
    List<Item> findByStorefrontId(Long storefrontId);

    List<Item> findByStorefrontIdAndIsActive(Long storefrontId, boolean isActive);

    Page<Item> findByStorefrontIdAndIsActive(Long storefrontId, boolean isActive, Pageable pageable);

    // Find by SKU
    Optional<Item> findBySku(String sku);

    Optional<Item> findBySkuAndIsActive(String sku, boolean isActive);

    // Find active items
    List<Item> findByIsActive(boolean isActive);

    Page<Item> findByIsActive(boolean isActive, Pageable pageable);

    // Find featured items
    List<Item> findByIsFeaturedAndIsActive(boolean isFeatured, boolean isActive);

    Page<Item> findByIsFeaturedAndIsActive(boolean isFeatured, boolean isActive, Pageable pageable);

    // Search items by name
    @Query("SELECT i FROM Item i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND i.isActive = true")
    Page<Item> searchByItemName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Search items by name and description
    @Query("SELECT i FROM Item i WHERE (LOWER(i.itemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(i.itemDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND i.isActive = true")
    Page<Item> searchByNameAndDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find by category
    List<Item> findByCategoryAndIsActive(String category, boolean isActive);

    Page<Item> findByCategoryAndIsActive(String category, boolean isActive, Pageable pageable);

    // Find by price range
    @Query("SELECT i FROM Item i WHERE i.price BETWEEN :minPrice AND :maxPrice AND i.isActive = true")
    Page<Item> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Find by storefront and category
    List<Item> findByStorefrontAndCategoryAndIsActive(Storefront storefront, String category, boolean isActive);

    Page<Item> findByStorefrontAndCategoryAndIsActive(Storefront storefront, String category, boolean isActive,
            Pageable pageable);

    // Find low stock items
    @Query("SELECT i FROM Item i WHERE i.stockQuantity <= i.lowStockThreshold AND i.isActive = true")
    List<Item> findLowStockItems();

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.stockQuantity <= i.lowStockThreshold AND i.isActive = true")
    List<Item> findLowStockItemsByStorefront(@Param("storefront") Storefront storefront);

    // Find out of stock items
    @Query("SELECT i FROM Item i WHERE i.stockQuantity = 0 AND i.isActive = true")
    List<Item> findOutOfStockItems();

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.stockQuantity = 0 AND i.isActive = true")
    List<Item> findOutOfStockItemsByStorefront(@Param("storefront") Storefront storefront);

    // Find items on sale
    @Query("SELECT i FROM Item i WHERE i.compareAtPrice IS NOT NULL AND i.compareAtPrice > i.price AND i.isActive = true")
    Page<Item> findItemsOnSale(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.compareAtPrice IS NOT NULL AND i.compareAtPrice > i.price AND i.isActive = true")
    Page<Item> findItemsOnSaleByStorefront(@Param("storefront") Storefront storefront, Pageable pageable);

    // Find top rated items
    @Query("SELECT i FROM Item i WHERE i.isActive = true ORDER BY i.averageRating DESC, i.totalReviews DESC")
    Page<Item> findTopRatedItems(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.isActive = true ORDER BY i.averageRating DESC, i.totalReviews DESC")
    Page<Item> findTopRatedItemsByStorefront(@Param("storefront") Storefront storefront, Pageable pageable);

    // Find best selling items
    @Query("SELECT i FROM Item i WHERE i.isActive = true ORDER BY i.totalSales DESC")
    Page<Item> findBestSellingItems(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.isActive = true ORDER BY i.totalSales DESC")
    Page<Item> findBestSellingItemsByStorefront(@Param("storefront") Storefront storefront, Pageable pageable);

    // Find recently added items
    @Query("SELECT i FROM Item i WHERE i.isActive = true ORDER BY i.createdAt DESC")
    Page<Item> findRecentlyAddedItems(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.storefront = :storefront AND i.isActive = true ORDER BY i.createdAt DESC")
    Page<Item> findRecentlyAddedItemsByStorefront(@Param("storefront") Storefront storefront, Pageable pageable);

    // Count items by storefront
    long countByStorefront(Storefront storefront);

    long countByStorefrontAndIsActive(Storefront storefront, boolean isActive);

    // Check if SKU exists
    boolean existsBySku(String sku);

    boolean existsBySkuAndStorefront(String sku, Storefront storefront);

    // Find items by owner user ID (through storefront)
    @Query("SELECT i FROM Item i WHERE (i.storefront.businessProfile.user.id = :userId OR i.storefront.sellerProfile.user.id = :userId)")
    List<Item> findByOwnerUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Item i WHERE (i.storefront.businessProfile.user.id = :userId OR i.storefront.sellerProfile.user.id = :userId) AND i.isActive = :isActive")
    List<Item> findByOwnerUserIdAndIsActive(@Param("userId") Long userId, @Param("isActive") boolean isActive);
}