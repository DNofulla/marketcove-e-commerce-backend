package com.dnofulla.marketcove.backend_api.controller;

import com.dnofulla.marketcove.backend_api.dto.item.CreateItemRequest;
import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import com.dnofulla.marketcove.backend_api.dto.item.UpdateItemRequest;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for item management
 * Accessible by BUSINESS_OWNER and SELLER roles
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Item Management", description = "APIs for managing items in storefronts")
public class ItemController {

    private final ItemService itemService;

    /**
     * Create a new item in a storefront
     */
    @PostMapping("/storefront/{storefrontId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Create new item", description = "Creates a new item in the specified storefront")
    public ResponseEntity<ItemResponse> createItem(
            @PathVariable Long storefrontId,
            @Valid @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Creating item in storefront {} for user: {}", storefrontId, currentUser.getEmail());
        ItemResponse response = itemService.createItem(storefrontId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing item
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Update item", description = "Updates an existing item owned by the authenticated user")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Updating item {} for user: {}", itemId, currentUser.getEmail());
        ItemResponse response = itemService.updateItem(itemId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Get item by ID
     */
    @GetMapping("/{itemId}")
    @Operation(summary = "Get item by ID", description = "Retrieves item details by ID")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long itemId) {
        ItemResponse response = itemService.getItem(itemId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get item by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get item by SKU", description = "Retrieves item details by SKU")
    public ResponseEntity<ItemResponse> getItemBySku(@PathVariable String sku) {
        ItemResponse response = itemService.getItemBySku(sku);
        return ResponseEntity.ok(response);
    }

    /**
     * Get items in a storefront
     */
    @GetMapping("/storefront/{storefrontId}")
    @Operation(summary = "Get storefront items", description = "Retrieves all items in a storefront")
    public ResponseEntity<Page<ItemResponse>> getStorefrontItems(
            @PathVariable Long storefrontId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getStorefrontItems(storefrontId, activeOnly, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get current user's items
     */
    @GetMapping("/my-items")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Get user's items", description = "Retrieves all items owned by the authenticated user")
    public ResponseEntity<List<ItemResponse>> getUserItems(@AuthenticationPrincipal User currentUser) {
        List<ItemResponse> items = itemService.getUserItems(currentUser);
        return ResponseEntity.ok(items);
    }

    /**
     * Search items
     */
    @GetMapping("/search")
    @Operation(summary = "Search items", description = "Searches items by name and description")
    public ResponseEntity<Page<ItemResponse>> searchItems(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.searchItems(query, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get items by category", description = "Retrieves items in a specific category")
    public ResponseEntity<Page<ItemResponse>> getItemsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getItemsByCategory(category, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get items by price range
     */
    @GetMapping("/price-range")
    @Operation(summary = "Get items by price range", description = "Retrieves items within a price range")
    public ResponseEntity<Page<ItemResponse>> getItemsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "price", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getItemsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get featured items
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured items", description = "Retrieves featured items")
    public ResponseEntity<Page<ItemResponse>> getFeaturedItems(
            @PageableDefault(size = 20, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getFeaturedItems(pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get items on sale
     */
    @GetMapping("/on-sale")
    @Operation(summary = "Get items on sale", description = "Retrieves items that are currently on sale")
    public ResponseEntity<Page<ItemResponse>> getItemsOnSale(
            @PageableDefault(size = 20, sort = "discountPercentage", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getItemsOnSale(pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get best selling items
     */
    @GetMapping("/best-selling")
    @Operation(summary = "Get best selling items", description = "Retrieves best selling items")
    public ResponseEntity<Page<ItemResponse>> getBestSellingItems(
            @PageableDefault(size = 20, sort = "totalSales", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getBestSellingItems(pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get recently added items
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recently added items", description = "Retrieves recently added items")
    public ResponseEntity<Page<ItemResponse>> getRecentlyAddedItems(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemResponse> items = itemService.getRecentlyAddedItems(pageable);
        return ResponseEntity.ok(items);
    }

    /**
     * Get low stock items for current user
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Get low stock items", description = "Retrieves items with low stock owned by the authenticated user")
    public ResponseEntity<List<ItemResponse>> getLowStockItems(@AuthenticationPrincipal User currentUser) {
        List<ItemResponse> items = itemService.getLowStockItems(currentUser);
        return ResponseEntity.ok(items);
    }

    /**
     * Delete an item
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Delete item", description = "Deletes an item owned by the authenticated user")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User currentUser) {

        log.info("Deleting item {} for user: {}", itemId, currentUser.getEmail());
        itemService.deleteItem(itemId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload item images
     */
    @PostMapping(value = "/{itemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Upload item images", description = "Uploads images for an item")
    public ResponseEntity<ItemResponse> uploadItemImages(
            @PathVariable Long itemId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal User currentUser) {

        log.info("Uploading {} images for item {} by user: {}", files.size(), itemId, currentUser.getEmail());
        ItemResponse response = itemService.uploadItemImages(itemId, files, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove item image
     */
    @DeleteMapping("/{itemId}/images")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Remove item image", description = "Removes an image from an item")
    public ResponseEntity<ItemResponse> removeItemImage(
            @PathVariable Long itemId,
            @RequestParam String imageUrl,
            @AuthenticationPrincipal User currentUser) {

        log.info("Removing image {} from item {} for user: {}", imageUrl, itemId, currentUser.getEmail());
        ItemResponse response = itemService.removeItemImage(itemId, imageUrl, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Update stock quantity
     */
    @PatchMapping("/{itemId}/stock")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Update stock quantity", description = "Updates the stock quantity for an item")
    public ResponseEntity<ItemResponse> updateStock(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            @AuthenticationPrincipal User currentUser) {

        log.info("Updating stock for item {} to {} by user: {}", itemId, quantity, currentUser.getEmail());
        ItemResponse response = itemService.updateStock(itemId, quantity, currentUser);
        return ResponseEntity.ok(response);
    }
}