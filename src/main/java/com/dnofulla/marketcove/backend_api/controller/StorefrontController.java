package com.dnofulla.marketcove.backend_api.controller;

import com.dnofulla.marketcove.backend_api.dto.storefront.CreateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.dto.storefront.StorefrontResponse;
import com.dnofulla.marketcove.backend_api.dto.storefront.UpdateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.service.StorefrontService;
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

import java.util.List;

/**
 * REST Controller for storefront management
 * Accessible by BUSINESS_OWNER and SELLER roles
 */
@RestController
@RequestMapping("/api/storefronts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Storefront Management", description = "APIs for managing storefronts")
public class StorefrontController {

    private final StorefrontService storefrontService;

    /**
     * Create a new storefront
     */
    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Create a new storefront", description = "Creates a new storefront for the authenticated business owner or seller")
    public ResponseEntity<StorefrontResponse> createStorefront(
            @Valid @RequestBody CreateStorefrontRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Creating storefront for user: {}", currentUser.getEmail());
        StorefrontResponse response = storefrontService.createStorefront(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing storefront
     */
    @PutMapping("/{storefrontId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Update storefront", description = "Updates an existing storefront owned by the authenticated user")
    public ResponseEntity<StorefrontResponse> updateStorefront(
            @PathVariable Long storefrontId,
            @Valid @RequestBody UpdateStorefrontRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Updating storefront {} for user: {}", storefrontId, currentUser.getEmail());
        StorefrontResponse response = storefrontService.updateStorefront(storefrontId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Get storefront by ID
     */
    @GetMapping("/{storefrontId}")
    @Operation(summary = "Get storefront by ID", description = "Retrieves storefront details by ID")
    public ResponseEntity<StorefrontResponse> getStorefront(@PathVariable Long storefrontId) {
        StorefrontResponse response = storefrontService.getStorefront(storefrontId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get storefront by URL slug
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get storefront by URL slug", description = "Retrieves storefront details by URL slug")
    public ResponseEntity<StorefrontResponse> getStorefrontBySlug(@PathVariable String slug) {
        StorefrontResponse response = storefrontService.getStorefrontBySlug(slug);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's storefronts
     */
    @GetMapping("/my-storefronts")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Get user's storefronts", description = "Retrieves all storefronts owned by the authenticated user")
    public ResponseEntity<List<StorefrontResponse>> getUserStorefronts(@AuthenticationPrincipal User currentUser) {
        List<StorefrontResponse> storefronts = storefrontService.getUserStorefronts(currentUser);
        return ResponseEntity.ok(storefronts);
    }

    /**
     * Get all active storefronts (public endpoint)
     */
    @GetMapping
    @Operation(summary = "Get active storefronts", description = "Retrieves all active storefronts with pagination")
    public ResponseEntity<Page<StorefrontResponse>> getActiveStorefronts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StorefrontResponse> storefronts = storefrontService.getActiveStorefronts(pageable);
        return ResponseEntity.ok(storefronts);
    }

    /**
     * Search storefronts by name
     */
    @GetMapping("/search")
    @Operation(summary = "Search storefronts", description = "Searches storefronts by name")
    public ResponseEntity<Page<StorefrontResponse>> searchStorefronts(
            @RequestParam String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StorefrontResponse> storefronts = storefrontService.searchStorefronts(query, pageable);
        return ResponseEntity.ok(storefronts);
    }

    /**
     * Get featured storefronts
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured storefronts", description = "Retrieves featured storefronts")
    public ResponseEntity<Page<StorefrontResponse>> getFeaturedStorefronts(
            @PageableDefault(size = 10, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StorefrontResponse> storefronts = storefrontService.getFeaturedStorefronts(pageable);
        return ResponseEntity.ok(storefronts);
    }

    /**
     * Get top rated storefronts
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated storefronts", description = "Retrieves top rated storefronts")
    public ResponseEntity<Page<StorefrontResponse>> getTopRatedStorefronts(
            @PageableDefault(size = 10, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StorefrontResponse> storefronts = storefrontService.getTopRatedStorefronts(pageable);
        return ResponseEntity.ok(storefronts);
    }

    /**
     * Delete a storefront
     */
    @DeleteMapping("/{storefrontId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Delete storefront", description = "Deletes a storefront owned by the authenticated user")
    public ResponseEntity<Void> deleteStorefront(
            @PathVariable Long storefrontId,
            @AuthenticationPrincipal User currentUser) {

        log.info("Deleting storefront {} for user: {}", storefrontId, currentUser.getEmail());
        storefrontService.deleteStorefront(storefrontId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload storefront logo
     */
    @PostMapping(value = "/{storefrontId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Upload storefront logo", description = "Uploads a logo image for the storefront")
    public ResponseEntity<StorefrontResponse> uploadStorefrontLogo(
            @PathVariable Long storefrontId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        log.info("Uploading logo for storefront {} by user: {}", storefrontId, currentUser.getEmail());
        StorefrontResponse response = storefrontService.uploadStorefrontLogo(storefrontId, file, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload storefront banner
     */
    @PostMapping(value = "/{storefrontId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('SELLER')")
    @Operation(summary = "Upload storefront banner", description = "Uploads a banner image for the storefront")
    public ResponseEntity<StorefrontResponse> uploadStorefrontBanner(
            @PathVariable Long storefrontId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        log.info("Uploading banner for storefront {} by user: {}", storefrontId, currentUser.getEmail());
        StorefrontResponse response = storefrontService.uploadStorefrontBanner(storefrontId, file, currentUser);
        return ResponseEntity.ok(response);
    }
}