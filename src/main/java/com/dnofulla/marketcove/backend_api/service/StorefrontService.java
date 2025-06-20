package com.dnofulla.marketcove.backend_api.service;

import com.dnofulla.marketcove.backend_api.dto.storefront.CreateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.dto.storefront.StorefrontResponse;
import com.dnofulla.marketcove.backend_api.dto.storefront.UpdateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.entity.BusinessProfile;
import com.dnofulla.marketcove.backend_api.entity.SellerProfile;
import com.dnofulla.marketcove.backend_api.entity.Storefront;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.repository.BusinessProfileRepository;
import com.dnofulla.marketcove.backend_api.repository.ItemRepository;
import com.dnofulla.marketcove.backend_api.repository.SellerProfileRepository;
import com.dnofulla.marketcove.backend_api.repository.StorefrontRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing storefronts
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StorefrontService {

    private final StorefrontRepository storefrontRepository;
    private final ItemRepository itemRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final S3ImageService s3ImageService;

    /**
     * Create a new storefront for the current user
     */
    public StorefrontResponse createStorefront(CreateStorefrontRequest request, User currentUser) {
        log.info("Creating storefront for user: {}", currentUser.getEmail());

        // Validate URL slug uniqueness
        if (request.getStoreUrlSlug() != null &&
                storefrontRepository.existsByStoreUrlSlug(request.getStoreUrlSlug())) {
            throw new IllegalArgumentException("Store URL slug already exists");
        }

        Storefront storefront = new Storefront();
        populateStorefrontFromRequest(storefront, request);

        // Set the owner based on user role
        setStorefrontOwner(storefront, currentUser);

        Storefront savedStorefront = storefrontRepository.save(storefront);
        log.info("Successfully created storefront with ID: {}", savedStorefront.getId());

        return convertToResponse(savedStorefront);
    }

    /**
     * Update an existing storefront
     */
    public StorefrontResponse updateStorefront(Long storefrontId, UpdateStorefrontRequest request, User currentUser) {
        log.info("Updating storefront {} for user: {}", storefrontId, currentUser.getEmail());

        Storefront storefront = getStorefrontById(storefrontId);
        validateOwnership(storefront, currentUser);

        // Validate URL slug uniqueness if changed
        if (request.getStoreUrlSlug() != null &&
                !request.getStoreUrlSlug().equals(storefront.getStoreUrlSlug()) &&
                storefrontRepository.existsByStoreUrlSlug(request.getStoreUrlSlug())) {
            throw new IllegalArgumentException("Store URL slug already exists");
        }

        updateStorefrontFromRequest(storefront, request);
        Storefront savedStorefront = storefrontRepository.save(storefront);

        log.info("Successfully updated storefront with ID: {}", savedStorefront.getId());
        return convertToResponse(savedStorefront);
    }

    /**
     * Get storefront by ID
     */
    @Transactional(readOnly = true)
    public StorefrontResponse getStorefront(Long storefrontId) {
        Storefront storefront = getStorefrontById(storefrontId);
        return convertToResponse(storefront);
    }

    /**
     * Get storefront by URL slug
     */
    @Transactional(readOnly = true)
    public StorefrontResponse getStorefrontBySlug(String slug) {
        Storefront storefront = storefrontRepository.findByStoreUrlSlugAndIsActive(slug, true)
                .orElseThrow(() -> new IllegalArgumentException("Storefront not found with slug: " + slug));
        return convertToResponse(storefront);
    }

    /**
     * Get all storefronts for the current user
     */
    @Transactional(readOnly = true)
    public List<StorefrontResponse> getUserStorefronts(User currentUser) {
        List<Storefront> storefronts = storefrontRepository.findByOwnerUserId(currentUser.getId());
        return storefronts.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Get all active storefronts with pagination
     */
    @Transactional(readOnly = true)
    public Page<StorefrontResponse> getActiveStorefronts(Pageable pageable) {
        return storefrontRepository.findByIsActive(true, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Search storefronts by name
     */
    @Transactional(readOnly = true)
    public Page<StorefrontResponse> searchStorefronts(String searchTerm, Pageable pageable) {
        return storefrontRepository.searchByStoreName(searchTerm, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get featured storefronts
     */
    @Transactional(readOnly = true)
    public Page<StorefrontResponse> getFeaturedStorefronts(Pageable pageable) {
        return storefrontRepository.findByIsFeaturedAndIsActive(true, true, pageable)
                .map(this::convertToResponse);
    }

    /**
     * Get top rated storefronts
     */
    @Transactional(readOnly = true)
    public Page<StorefrontResponse> getTopRatedStorefronts(Pageable pageable) {
        return storefrontRepository.findTopRatedStorefronts(pageable)
                .map(this::convertToResponse);
    }

    /**
     * Delete a storefront (soft delete by setting isActive to false)
     */
    public void deleteStorefront(Long storefrontId, User currentUser) {
        log.info("Deleting storefront {} for user: {}", storefrontId, currentUser.getEmail());

        Storefront storefront = getStorefrontById(storefrontId);
        validateOwnership(storefront, currentUser);

        storefront.setActive(false);
        storefrontRepository.save(storefront);

        log.info("Successfully deleted storefront with ID: {}", storefrontId);
    }

    /**
     * Upload storefront logo
     */
    public StorefrontResponse uploadStorefrontLogo(Long storefrontId, MultipartFile file, User currentUser) {
        log.info("Uploading logo for storefront {}", storefrontId);

        Storefront storefront = getStorefrontById(storefrontId);
        validateOwnership(storefront, currentUser);

        if (!s3ImageService.isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        if (file.getSize() > s3ImageService.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }

        // Delete old logo if exists
        if (storefront.getStoreLogoUrl() != null) {
            s3ImageService.deleteImage(storefront.getStoreLogoUrl());
        }

        String imageUrl = s3ImageService.uploadImage(file, "storefronts/logos");
        if (imageUrl != null) {
            storefront.setStoreLogoUrl(imageUrl);
            storefront = storefrontRepository.save(storefront);
        }

        return convertToResponse(storefront);
    }

    /**
     * Upload storefront banner
     */
    public StorefrontResponse uploadStorefrontBanner(Long storefrontId, MultipartFile file, User currentUser) {
        log.info("Uploading banner for storefront {}", storefrontId);

        Storefront storefront = getStorefrontById(storefrontId);
        validateOwnership(storefront, currentUser);

        if (!s3ImageService.isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file");
        }

        if (file.getSize() > s3ImageService.getMaxFileSize()) {
            throw new IllegalArgumentException("File size exceeds maximum limit");
        }

        // Delete old banner if exists
        if (storefront.getStoreBannerUrl() != null) {
            s3ImageService.deleteImage(storefront.getStoreBannerUrl());
        }

        String imageUrl = s3ImageService.uploadImage(file, "storefronts/banners");
        if (imageUrl != null) {
            storefront.setStoreBannerUrl(imageUrl);
            storefront = storefrontRepository.save(storefront);
        }

        return convertToResponse(storefront);
    }

    // Private helper methods

    private Storefront getStorefrontById(Long storefrontId) {
        return storefrontRepository.findById(storefrontId)
                .orElseThrow(() -> new IllegalArgumentException("Storefront not found with ID: " + storefrontId));
    }

    private void validateOwnership(Storefront storefront, User currentUser) {
        User owner = storefront.getOwnerUser();
        if (owner == null || !owner.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this storefront");
        }
    }

    private void setStorefrontOwner(Storefront storefront, User user) {
        if (user.getRole() == UserRole.BUSINESS_OWNER) {
            BusinessProfile businessProfile = businessProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalStateException("Business profile not found for user"));
            storefront.setBusinessProfile(businessProfile);
        } else if (user.getRole() == UserRole.SELLER) {
            SellerProfile sellerProfile = sellerProfileRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalStateException("Seller profile not found for user"));
            storefront.setSellerProfile(sellerProfile);
        } else {
            throw new IllegalArgumentException("Only business owners and sellers can create storefronts");
        }
    }

    private void populateStorefrontFromRequest(Storefront storefront, CreateStorefrontRequest request) {
        storefront.setStoreName(request.getStoreName());
        storefront.setStoreDescription(request.getStoreDescription());
        storefront.setStoreUrlSlug(request.getStoreUrlSlug());
        storefront.setContactEmail(request.getContactEmail());
        storefront.setContactPhone(request.getContactPhone());
        storefront.setReturnPolicy(request.getReturnPolicy());
        storefront.setShippingPolicy(request.getShippingPolicy());
        storefront.setFacebookUrl(request.getFacebookUrl());
        storefront.setTwitterUrl(request.getTwitterUrl());
        storefront.setInstagramUrl(request.getInstagramUrl());
        storefront.setWebsiteUrl(request.getWebsiteUrl());
    }

    private void updateStorefrontFromRequest(Storefront storefront, UpdateStorefrontRequest request) {
        if (request.getStoreName() != null) {
            storefront.setStoreName(request.getStoreName());
        }
        if (request.getStoreDescription() != null) {
            storefront.setStoreDescription(request.getStoreDescription());
        }
        if (request.getStoreUrlSlug() != null) {
            storefront.setStoreUrlSlug(request.getStoreUrlSlug());
        }
        if (request.getContactEmail() != null) {
            storefront.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            storefront.setContactPhone(request.getContactPhone());
        }
        if (request.getReturnPolicy() != null) {
            storefront.setReturnPolicy(request.getReturnPolicy());
        }
        if (request.getShippingPolicy() != null) {
            storefront.setShippingPolicy(request.getShippingPolicy());
        }
        if (request.getIsActive() != null) {
            storefront.setActive(request.getIsActive());
        }
        if (request.getIsFeatured() != null) {
            storefront.setFeatured(request.getIsFeatured());
        }
        if (request.getFacebookUrl() != null) {
            storefront.setFacebookUrl(request.getFacebookUrl());
        }
        if (request.getTwitterUrl() != null) {
            storefront.setTwitterUrl(request.getTwitterUrl());
        }
        if (request.getInstagramUrl() != null) {
            storefront.setInstagramUrl(request.getInstagramUrl());
        }
        if (request.getWebsiteUrl() != null) {
            storefront.setWebsiteUrl(request.getWebsiteUrl());
        }
    }

    private StorefrontResponse convertToResponse(Storefront storefront) {
        StorefrontResponse response = new StorefrontResponse();
        response.setId(storefront.getId());
        response.setStoreName(storefront.getStoreName());
        response.setStoreDescription(storefront.getStoreDescription());
        response.setStoreUrlSlug(storefront.getStoreUrlSlug());
        response.setStoreLogoUrl(storefront.getStoreLogoUrl());
        response.setStoreBannerUrl(storefront.getStoreBannerUrl());
        response.setContactEmail(storefront.getContactEmail());
        response.setContactPhone(storefront.getContactPhone());
        response.setReturnPolicy(storefront.getReturnPolicy());
        response.setShippingPolicy(storefront.getShippingPolicy());
        response.setActive(storefront.isActive());
        response.setFeatured(storefront.isFeatured());
        response.setAverageRating(storefront.getAverageRating());
        response.setTotalReviews(storefront.getTotalReviews());
        response.setTotalSales(storefront.getTotalSales());
        response.setTotalOrders(storefront.getTotalOrders());
        response.setFacebookUrl(storefront.getFacebookUrl());
        response.setTwitterUrl(storefront.getTwitterUrl());
        response.setInstagramUrl(storefront.getInstagramUrl());
        response.setWebsiteUrl(storefront.getWebsiteUrl());
        response.setOwnerName(storefront.getOwnerName());
        response.setOwnerType(storefront.getOwnerType());
        response.setCreatedAt(storefront.getCreatedAt());
        response.setUpdatedAt(storefront.getUpdatedAt());

        // Count total items
        int totalItems = (int) itemRepository.countByStorefrontAndIsActive(storefront, true);
        response.setTotalItems(totalItems);

        return response;
    }
}