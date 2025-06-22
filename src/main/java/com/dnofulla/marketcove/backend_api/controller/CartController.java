package com.dnofulla.marketcove.backend_api.controller;

import com.dnofulla.marketcove.backend_api.dto.cart.AddToCartRequest;
import com.dnofulla.marketcove.backend_api.dto.cart.CartResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.UpdateCartItemRequest;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for shopping cart management
 * Accessible by authenticated customers
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart for authenticated customers")
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get cart", description = "Retrieves the current user's shopping cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User currentUser) {
        log.info("Getting cart for user: {}", currentUser.getEmail());
        CartResponse response = cartService.getCart(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add item to cart", description = "Adds an item to the current user's shopping cart")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Adding item {} to cart for user: {}", request.getItemId(), currentUser.getEmail());
        CartResponse response = cartService.addToCart(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update cart item", description = "Updates the quantity of a cart item")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("Updating cart item {} for user: {}", cartItemId, currentUser.getEmail());
        CartResponse response = cartService.updateCartItem(cartItemId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the current user's shopping cart")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal User currentUser) {

        log.info("Removing cart item {} for user: {}", cartItemId, currentUser.getEmail());
        CartResponse response = cartService.removeFromCart(cartItemId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear entire cart
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Clear cart", description = "Removes all items from the current user's shopping cart")
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal User currentUser) {
        log.info("Clearing cart for user: {}", currentUser.getEmail());
        CartResponse response = cartService.clearCart(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Get cart item count
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get cart item count", description = "Gets the total number of items in the current user's cart")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal User currentUser) {
        int count = cartService.getCartItemCount(currentUser);
        return ResponseEntity.ok(count);
    }
}