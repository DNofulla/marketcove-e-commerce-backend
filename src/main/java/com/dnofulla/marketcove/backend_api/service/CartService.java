package com.dnofulla.marketcove.backend_api.service;

import com.dnofulla.marketcove.backend_api.dto.cart.AddToCartRequest;
import com.dnofulla.marketcove.backend_api.dto.cart.CartItemResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.CartResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.UpdateCartItemRequest;
import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import com.dnofulla.marketcove.backend_api.entity.Cart;
import com.dnofulla.marketcove.backend_api.entity.CartItem;
import com.dnofulla.marketcove.backend_api.entity.Item;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.repository.CartItemRepository;
import com.dnofulla.marketcove.backend_api.repository.CartRepository;
import com.dnofulla.marketcove.backend_api.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing shopping cart operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    /**
     * Get user's cart
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        log.info("Getting cart for user: {}", user.getEmail());
        Cart cart = getOrCreateCart(user);
        return convertToCartResponse(cart);
    }

    /**
     * Add item to cart
     */
    public CartResponse addToCart(AddToCartRequest request, User user) {
        log.info("Adding item {} to cart for user: {}", request.getItemId(), user.getEmail());

        // Validate item exists and is active
        Item item = getActiveItem(request.getItemId());

        // Check if item has sufficient stock
        if (item.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + item.getStockQuantity());
        }

        Cart cart = getOrCreateCart(user);

        // Check if item already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndItem(cart, item);

        if (existingCartItem.isPresent()) {
            // Update quantity if item already in cart
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            // Check total quantity against stock
            if (item.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Available: " +
                        item.getStockQuantity() + ", Currently in cart: " + cartItem.getQuantity());
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtTime(item.getPrice());

            cartItemRepository.save(cartItem);
            cart.addItem(cartItem);
        }

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        log.info("Successfully added item to cart. Cart now has {} items", savedCart.getTotalItems());
        return convertToCartResponse(savedCart);
    }

    /**
     * Update cart item quantity
     */
    public CartResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request, User user) {
        log.info("Updating cart item {} quantity to {} for user: {}", cartItemId, request.getQuantity(),
                user.getEmail());

        CartItem cartItem = getCartItemById(cartItemId);
        validateCartItemOwnership(cartItem, user);

        // Check stock availability
        if (cartItem.getItem().getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + cartItem.getItem().getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        log.info("Successfully updated cart item quantity");
        return convertToCartResponse(savedCart);
    }

    /**
     * Remove item from cart
     */
    public CartResponse removeFromCart(Long cartItemId, User user) {
        log.info("Removing cart item {} for user: {}", cartItemId, user.getEmail());

        CartItem cartItem = getCartItemById(cartItemId);
        validateCartItemOwnership(cartItem, user);

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        log.info("Successfully removed item from cart");
        return convertToCartResponse(savedCart);
    }

    /**
     * Clear all items from cart
     */
    public CartResponse clearCart(User user) {
        log.info("Clearing cart for user: {}", user.getEmail());

        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCart(cart);
        cart.clearCart();
        Cart savedCart = cartRepository.save(cart);

        log.info("Successfully cleared cart");
        return convertToCartResponse(savedCart);
    }

    /**
     * Get cart item count for user
     */
    @Transactional(readOnly = true)
    public int getCartItemCount(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        return cart != null ? cart.getTotalItems() : 0;
    }

    // Helper methods

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> createNewCart(user));
    }

    private Cart createNewCart(User user) {
        log.info("Creating new cart for user: {}", user.getEmail());
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Item getActiveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

        if (!item.isActive()) {
            throw new IllegalArgumentException("Item is not available for purchase");
        }

        return item;
    }

    private CartItem getCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found with ID: " + cartItemId));
    }

    private void validateCartItemOwnership(CartItem cartItem, User user) {
        if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to cart item");
        }
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(cartItemResponses)
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .empty(cart.isEmpty())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        ItemResponse itemResponse = itemService.convertToResponse(cartItem.getItem());

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .item(itemResponse)
                .quantity(cartItem.getQuantity())
                .priceAtTime(cartItem.getPriceAtTime())
                .subtotal(cartItem.getSubtotal())
                .priceChanged(cartItem.isPriceChanged())
                .currentItemPrice(cartItem.getCurrentItemPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}