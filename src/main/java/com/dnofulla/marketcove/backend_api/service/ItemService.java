package com.dnofulla.marketcove.backend_api.service;

import com.dnofulla.marketcove.backend_api.dto.item.CreateItemRequest;
import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import com.dnofulla.marketcove.backend_api.dto.item.UpdateItemRequest;
import com.dnofulla.marketcove.backend_api.entity.Item;
import com.dnofulla.marketcove.backend_api.entity.Storefront;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.repository.ItemRepository;
import com.dnofulla.marketcove.backend_api.repository.StorefrontRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing items
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final StorefrontRepository storefrontRepository;
    private final S3ImageService s3ImageService;

    /**
     * Create a new item in a storefront
     */
    public ItemResponse createItem(Long storefrontId, CreateItemRequest request, User currentUser) {
        log.info("Creating item in storefront {} for user: {}", storefrontId, currentUser.getEmail());

        Storefront storefront = getStorefrontById(storefrontId);
        validateStorefrontOwnership(storefront, currentUser);

        // Validate SKU uniqueness within the storefront
        if (request.getSku() != null &&
                itemRepository.existsBySkuAndStorefront(request.getSku(), storefront)) {
            throw new IllegalArgumentException("SKU already exists in this storefront");
        }

        Item item = new Item();
        populateItemFromRequest(item, request);
        item.setStorefront(storefront);

        Item savedItem = itemRepository.save(item);
        log.info("Successfully created item with ID: {}", savedItem.getId());

        return convertToResponse(savedItem);
    }

    /**
     * Update an existing item
     */
    public ItemResponse updateItem(Long itemId, UpdateItemRequest request, User currentUser) {
        log.info("Updating item {} for user: {}", itemId, currentUser.getEmail());

        Item item = getItemById(itemId);
        validateItemOwnership(item, currentUser);

        // Validate SKU uniqueness if changed
        if (request.getSku() != null &&
                !request.getSku().equals(item.getSku()) &&
                itemRepository.existsBySkuAndStorefront(request.getSku(), item.getStorefront())) {
            throw new IllegalArgumentException("SKU already exists in this storefront");
        }

        updateItemFromRequest(item, request);
        Item savedItem = itemRepository.save(item);

        log.info("Successfully updated item with ID: {}", savedItem.getId());
        return convertToResponse(savedItem);
    }

    /**
     * Get item by ID
     */
    @Transactional(readOnly = true)
    public ItemResponse getItem(Long itemId) {
        Item item = getItemById(itemId);

        // Increment view count
        item.incrementViewCount();
        itemRepository.save(item);

        return convertToResponse(item);
    }

    /**
     * Get item by SKU
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemBySku(String sku) {
        Item item = itemRepository.findBySkuAndIsActive(sku, true)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with SKU: " + sku));

        // Increment view count
        item.incrementViewCount();
        itemRepository.save(item);

        return convertToResponse(item);
    }

    /**
     * Get all items in a storefront
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getStorefrontItems(Long storefrontId, boolean activeOnly, Pageable pageable) {
        if (activeOnly) {
            return itemRepository.findByStorefrontIdAndIsActive(storefrontId, true, pageable)
                    .map(this::convertToResponse);
        } else {
            List<Item> items = itemRepository.findByStorefrontId(storefrontId);
            List<ItemResponse> responses = items.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // Simple pagination simulation for non-active filter
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), responses.size());
            List<ItemResponse> pageContent = responses.subList(start, end);

            return new PageImpl<>(pageContent, pageable, responses.size());
        }
    }

    /**
     * Get all items for the current user
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getUserItems(User currentUser) {
        List<Item> items = itemRepository.findByOwnerUserId(currentUser.getId());
        return items.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Search items by name and description
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> searchItems(String searchTerm, Pageable pageable) {
        return itemRepository.searchByNameAndDescription(searchTerm, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get items by category
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsByCategory(String category, Pageable pageable) {
        return itemRepository.findByCategoryAndIsActive(category, true, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get items by price range
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return itemRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get featured items
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getFeaturedItems(Pageable pageable) {
        return itemRepository.findByIsFeaturedAndIsActive(true, true, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get items on sale
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemsOnSale(Pageable pageable) {
        return itemRepository.findItemsOnSale(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get best selling items
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getBestSellingItems(Pageable pageable) {
        return itemRepository.findBestSellingItems(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get recently added items
     */
    @Transactional(readOnly = true)
    public Page<ItemResponse> getRecentlyAddedItems(Pageable pageable) {
        return itemRepository.findRecentlyAddedItems(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get low stock items for the current user
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> getLowStockItems(User currentUser) {
        List<Item> userItems = itemRepository.findByOwnerUserIdAndIsActive(currentUser.getId(), true);
        return userItems.stream()
                .filter(Item::isLowStock)
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Delete an item (soft delete by setting isActive to false)
     */
    public void deleteItem(Long itemId, User currentUser) {
        log.info("Deleting item {} for user: {}", itemId, currentUser.getEmail());

        Item item = getItemById(itemId);
        validateItemOwnership(item, currentUser);

        item.setActive(false);
        itemRepository.save(item);

        log.info("Successfully deleted item with ID: {}", itemId);
    }

    /**
     * Upload item images
     */
    public ItemResponse uploadItemImages(Long itemId, List<MultipartFile> files, User currentUser) {
        log.info("Uploading {} images for item {}", files.size(), itemId);

        Item item = getItemById(itemId);
        validateItemOwnership(item, currentUser);

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!s3ImageService.isValidImageFile(file)) {
                throw new IllegalArgumentException("Invalid image file: " + file.getOriginalFilename());
            }

            if (file.getSize() > s3ImageService.getMaxFileSize()) {
                throw new IllegalArgumentException("File size exceeds maximum limit: " + file.getOriginalFilename());
            }

            String imageUrl = s3ImageService.uploadImage(file, "items");
            if (imageUrl != null) {
                imageUrls.add(imageUrl);
            }
        }

        // Add new image URLs to existing ones
        List<String> existingUrls = item.getImageUrls();
        if (existingUrls == null) {
            existingUrls = new ArrayList<>();
        }
        existingUrls.addAll(imageUrls);
        item.setImageUrls(existingUrls);

        // Set primary image if not set
        if (item.getPrimaryImageUrl() == null && !existingUrls.isEmpty()) {
            item.setPrimaryImageUrl(existingUrls.get(0));
        }

        item = itemRepository.save(item);
        return convertToResponse(item);
    }

    /**
     * Remove item image
     */
    public ItemResponse removeItemImage(Long itemId, String imageUrl, User currentUser) {
        log.info("Removing image {} from item {}", imageUrl, itemId);

        Item item = getItemById(itemId);
        validateItemOwnership(item, currentUser);

        List<String> imageUrls = item.getImageUrls();
        if (imageUrls != null && imageUrls.contains(imageUrl)) {
            imageUrls.remove(imageUrl);
            item.setImageUrls(imageUrls);

            // Update primary image if the removed image was primary
            if (imageUrl.equals(item.getPrimaryImageUrl())) {
                item.setPrimaryImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
            }

            // Delete from S3
            s3ImageService.deleteImage(imageUrl);

            item = itemRepository.save(item);
        }

        return convertToResponse(item);
    }

    /**
     * Update stock quantity
     */
    public ItemResponse updateStock(Long itemId, int quantity, User currentUser) {
        log.info("Updating stock for item {} to {}", itemId, quantity);

        Item item = getItemById(itemId);
        validateItemOwnership(item, currentUser);

        item.setStockQuantity(quantity);
        item = itemRepository.save(item);

        return convertToResponse(item);
    }

    // Private helper methods

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));
    }

    private Storefront getStorefrontById(Long storefrontId) {
        return storefrontRepository.findById(storefrontId)
                .orElseThrow(() -> new IllegalArgumentException("Storefront not found with ID: " + storefrontId));
    }

    private void validateStorefrontOwnership(Storefront storefront, User currentUser) {
        User owner = storefront.getOwnerUser();
        if (owner == null || !owner.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this storefront");
        }
    }

    private void validateItemOwnership(Item item, User currentUser) {
        User owner = item.getStorefront().getOwnerUser();
        if (owner == null || !owner.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this item");
        }
    }

    private void populateItemFromRequest(Item item, CreateItemRequest request) {
        item.setItemName(request.getItemName());
        item.setItemDescription(request.getItemDescription());
        item.setSku(request.getSku());
        item.setPrice(request.getPrice());
        item.setCompareAtPrice(request.getCompareAtPrice());
        item.setStockQuantity(request.getStockQuantity());
        item.setLowStockThreshold(request.getLowStockThreshold());
        item.setWeight(request.getWeight());
        item.setWeightUnit(request.getWeightUnit());
        item.setCategory(request.getCategory());
        item.setTags(request.getTags());
        item.setRequiresShipping(request.getRequiresShipping());
        item.setDigital(request.getIsDigital());
        item.setSeoTitle(request.getSeoTitle());
        item.setSeoDescription(request.getSeoDescription());
        item.setImageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>());
    }

    private void updateItemFromRequest(Item item, UpdateItemRequest request) {
        if (request.getItemName() != null) {
            item.setItemName(request.getItemName());
        }
        if (request.getItemDescription() != null) {
            item.setItemDescription(request.getItemDescription());
        }
        if (request.getSku() != null) {
            item.setSku(request.getSku());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCompareAtPrice() != null) {
            item.setCompareAtPrice(request.getCompareAtPrice());
        }
        if (request.getStockQuantity() != null) {
            item.setStockQuantity(request.getStockQuantity());
        }
        if (request.getLowStockThreshold() != null) {
            item.setLowStockThreshold(request.getLowStockThreshold());
        }
        if (request.getWeight() != null) {
            item.setWeight(request.getWeight());
        }
        if (request.getWeightUnit() != null) {
            item.setWeightUnit(request.getWeightUnit());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            item.setTags(request.getTags());
        }
        if (request.getIsActive() != null) {
            item.setActive(request.getIsActive());
        }
        if (request.getIsFeatured() != null) {
            item.setFeatured(request.getIsFeatured());
        }
        if (request.getRequiresShipping() != null) {
            item.setRequiresShipping(request.getRequiresShipping());
        }
        if (request.getIsDigital() != null) {
            item.setDigital(request.getIsDigital());
        }
        if (request.getSeoTitle() != null) {
            item.setSeoTitle(request.getSeoTitle());
        }
        if (request.getSeoDescription() != null) {
            item.setSeoDescription(request.getSeoDescription());
        }
        if (request.getImageUrls() != null) {
            item.setImageUrls(request.getImageUrls());
        }
    }

    public ItemResponse convertToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setItemDescription(item.getItemDescription());
        response.setSku(item.getSku());
        response.setPrice(item.getPrice());
        response.setCompareAtPrice(item.getCompareAtPrice());
        response.setStockQuantity(item.getStockQuantity());
        response.setLowStockThreshold(item.getLowStockThreshold());
        response.setWeight(item.getWeight());
        response.setWeightUnit(item.getWeightUnit());
        response.setCategory(item.getCategory());
        response.setTags(item.getTags());
        response.setActive(item.isActive());
        response.setFeatured(item.isFeatured());
        response.setRequiresShipping(item.isRequiresShipping());
        response.setDigital(item.isDigital());
        response.setSeoTitle(item.getSeoTitle());
        response.setSeoDescription(item.getSeoDescription());
        response.setAverageRating(item.getAverageRating());
        response.setTotalReviews(item.getTotalReviews());
        response.setTotalSales(item.getTotalSales());
        response.setViewCount(item.getViewCount());
        response.setImageUrls(item.getImageUrls());
        response.setPrimaryImageUrl(item.getPrimaryImageUrl());
        response.setStorefrontId(item.getStorefront().getId());
        response.setStorefrontName(item.getStorefront().getStoreName());
        response.setOnSale(item.isOnSale());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setDiscountPercentage(item.getDiscountPercentage());
        response.setLowStock(item.isLowStock());
        response.setOutOfStock(item.isOutOfStock());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());

        return response;
    }
}