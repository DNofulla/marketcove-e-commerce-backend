package com.dnofulla.marketcove.backend_api;

import com.dnofulla.marketcove.backend_api.dto.cart.AddToCartRequest;
import com.dnofulla.marketcove.backend_api.dto.cart.CartResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.UpdateCartItemRequest;
import com.dnofulla.marketcove.backend_api.entity.*;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.repository.CartItemRepository;
import com.dnofulla.marketcove.backend_api.repository.CartRepository;
import com.dnofulla.marketcove.backend_api.repository.ItemRepository;
import com.dnofulla.marketcove.backend_api.service.CartService;
import com.dnofulla.marketcove.backend_api.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for CartService business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private CartService cartService;

    private User mockCustomer;
    private Item mockItem;
    private Cart mockCart;
    private CartItem mockCartItem;
    private Storefront mockStorefront;
    private User mockSeller;

    @BeforeEach
    void setUp() {
        // Setup mock customer
        mockCustomer = new User();
        mockCustomer.setId(1L);
        mockCustomer.setEmail("customer@test.com");
        mockCustomer.setFirstName("John");
        mockCustomer.setLastName("Doe");
        mockCustomer.setRole(UserRole.CUSTOMER);

        // Setup mock seller
        mockSeller = new User();
        mockSeller.setId(2L);
        mockSeller.setEmail("seller@test.com");
        mockSeller.setRole(UserRole.SELLER);

        // Setup mock seller profile
        SellerProfile sellerProfile = new SellerProfile();
        sellerProfile.setUser(mockSeller);
        sellerProfile.setShopName("Test Store");

        // Setup mock storefront
        mockStorefront = new Storefront();
        mockStorefront.setId(1L);
        mockStorefront.setStoreName("Test Store");
        mockStorefront.setSellerProfile(sellerProfile);

        // Setup mock item
        mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setItemName("Test Item");
        mockItem.setPrice(BigDecimal.valueOf(19.99));
        mockItem.setStockQuantity(10);
        mockItem.setActive(true);
        mockItem.setStorefront(mockStorefront);

        // Setup mock cart
        mockCart = new Cart();
        mockCart.setId(1L);
        mockCart.setUser(mockCustomer);
        mockCart.setCartItems(new ArrayList<>());
        mockCart.setTotalAmount(BigDecimal.ZERO);
        mockCart.setTotalItems(0);
        mockCart.setCreatedAt(LocalDateTime.now());
        mockCart.setUpdatedAt(LocalDateTime.now());

        // Setup mock cart item
        mockCartItem = new CartItem();
        mockCartItem.setId(1L);
        mockCartItem.setCart(mockCart);
        mockCartItem.setItem(mockItem);
        mockCartItem.setQuantity(2);
        mockCartItem.setPriceAtTime(BigDecimal.valueOf(19.99));
        mockCartItem.setCreatedAt(LocalDateTime.now());
        mockCartItem.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Get Cart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should get existing cart with items")
        void testGetExistingCartWithItems() {
            // Arrange
            List<CartItem> cartItems = List.of(mockCartItem);
            mockCart.setCartItems(cartItems);
            mockCart.setTotalAmount(BigDecimal.valueOf(39.98));
            mockCart.setTotalItems(2);

            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
            when(itemService.convertToResponse(mockItem))
                    .thenReturn(new com.dnofulla.marketcove.backend_api.dto.item.ItemResponse());

            // Act
            CartResponse result = cartService.getCart(mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getTotalItems()).isEqualTo(2);
            assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(39.98));
            assertThat(result.isEmpty()).isFalse();
            assertThat(result.getCartItems()).hasSize(1);

            verify(cartRepository, times(1)).findByUserIdWithItems(1L);
        }

        @Test
        @DisplayName("Should create new cart when user has no cart")
        void testCreateNewCartForUser() {
            // Arrange
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.getCart(mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getTotalItems()).isEqualTo(0);
            assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getCartItems()).isEmpty();

            verify(cartRepository, times(1)).findByUserIdWithItems(1L);
            verify(cartRepository, times(1)).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("Add to Cart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should add new item to cart successfully")
        void testAddNewItemToCartSuccess() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(1L, 2);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
            when(cartItemRepository.findByCartAndItem(mockCart, mockItem)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);
            when(itemService.convertToResponse(mockItem))
                    .thenReturn(new com.dnofulla.marketcove.backend_api.dto.item.ItemResponse());

            // Act
            CartResponse result = cartService.addToCart(request, mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            verify(itemRepository, times(1)).findById(1L);
            verify(cartItemRepository, times(1)).save(any(CartItem.class));
            verify(cartRepository, times(1)).save(any(Cart.class));
        }

        @Test
        @DisplayName("Should update quantity when item already exists in cart")
        void testUpdateExistingItemQuantity() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(1L, 3);
            mockCartItem.setQuantity(2); // Current quantity

            when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
            when(cartItemRepository.findByCartAndItem(mockCart, mockItem)).thenReturn(Optional.of(mockCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.addToCart(request, mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(mockCartItem.getQuantity()).isEqualTo(5); // 2 + 3
            verify(cartItemRepository, times(1)).save(mockCartItem);
        }

        @Test
        @DisplayName("Should throw exception when item not found")
        void testAddToCartItemNotFound() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(999L, 2);

            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Item not found with ID: 999");

            verify(itemRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when item is not active")
        void testAddToCartInactiveItem() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(1L, 2);
            mockItem.setActive(false);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Item is not available for purchase");

            verify(itemRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void testAddToCartInsufficientStock() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(1L, 15); // More than stock
            mockItem.setStockQuantity(10);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient stock. Available: 10");

            verify(itemRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when total quantity exceeds stock")
        void testAddToCartTotalQuantityExceedsStock() {
            // Arrange
            AddToCartRequest request = new AddToCartRequest(1L, 6);
            mockCartItem.setQuantity(5); // Already 5 in cart, trying to add 6 more (11 total > 10 stock)
            mockItem.setStockQuantity(10);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem));
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
            when(cartItemRepository.findByCartAndItem(mockCart, mockItem)).thenReturn(Optional.of(mockCartItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient stock. Available: 10, Currently in cart: 5");
        }
    }

    @Nested
    @DisplayName("Update Cart Item Tests")
    class UpdateCartItemTests {

        @Test
        @DisplayName("Should update cart item quantity successfully")
        void testUpdateCartItemSuccess() {
            // Arrange
            UpdateCartItemRequest request = new UpdateCartItemRequest(5);

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(mockCartItem);
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.updateCartItem(1L, request, mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(mockCartItem.getQuantity()).isEqualTo(5);
            verify(cartItemRepository, times(1)).save(mockCartItem);
            verify(cartRepository, times(1)).save(mockCart);
        }

        @Test
        @DisplayName("Should throw exception when cart item not found")
        void testUpdateCartItemNotFound() {
            // Arrange
            UpdateCartItemRequest request = new UpdateCartItemRequest(5);

            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCartItem(999L, request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cart item not found with ID: 999");

            verify(cartItemRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when unauthorized access")
        void testUpdateCartItemUnauthorized() {
            // Arrange
            UpdateCartItemRequest request = new UpdateCartItemRequest(5);
            User otherUser = new User();
            otherUser.setId(999L);
            mockCartItem.getCart().setUser(otherUser);

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCartItem(1L, request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unauthorized access to cart item");

            verify(cartItemRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void testUpdateCartItemInsufficientStock() {
            // Arrange
            UpdateCartItemRequest request = new UpdateCartItemRequest(15); // More than stock
            mockItem.setStockQuantity(10);

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCartItem(1L, request, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient stock. Available: 10");

            verify(cartItemRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Remove from Cart Tests")
    class RemoveFromCartTests {

        @Test
        @DisplayName("Should remove item from cart successfully")
        void testRemoveFromCartSuccess() {
            // Arrange
            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.removeFromCart(1L, mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            verify(cartItemRepository, times(1)).delete(mockCartItem);
            verify(cartRepository, times(1)).save(mockCart);
        }

        @Test
        @DisplayName("Should throw exception when cart item not found")
        void testRemoveFromCartItemNotFound() {
            // Arrange
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.removeFromCart(999L, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cart item not found with ID: 999");

            verify(cartItemRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should throw exception when unauthorized access")
        void testRemoveFromCartUnauthorized() {
            // Arrange
            User otherUser = new User();
            otherUser.setId(999L);
            mockCartItem.getCart().setUser(otherUser);

            when(cartItemRepository.findById(1L)).thenReturn(Optional.of(mockCartItem));

            // Act & Assert
            assertThatThrownBy(() -> cartService.removeFromCart(1L, mockCustomer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unauthorized access to cart item");

            verify(cartItemRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("Clear Cart Tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear cart successfully")
        void testClearCartSuccess() {
            // Arrange
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(mockCart));
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.clearCart(mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getTotalItems()).isEqualTo(0);
            assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);

            verify(cartItemRepository, times(1)).deleteByCart(mockCart);
            verify(cartRepository, times(1)).save(mockCart);
        }

        @Test
        @DisplayName("Should create new cart and clear when user has no cart")
        void testClearCartCreateNew() {
            // Arrange
            when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);

            // Act
            CartResponse result = cartService.clearCart(mockCustomer);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
            verify(cartRepository, times(2)).save(any(Cart.class)); // Once for creation, once for clearing
        }
    }

    @Nested
    @DisplayName("Get Cart Item Count Tests")
    class GetCartItemCountTests {

        @Test
        @DisplayName("Should return correct item count for existing cart")
        void testGetCartItemCountExistingCart() {
            // Arrange
            mockCart.setTotalItems(5);
            when(cartRepository.findByUser(mockCustomer)).thenReturn(Optional.of(mockCart));

            // Act
            int result = cartService.getCartItemCount(mockCustomer);

            // Assert
            assertThat(result).isEqualTo(5);
            verify(cartRepository, times(1)).findByUser(mockCustomer);
        }

        @Test
        @DisplayName("Should return 0 for user with no cart")
        void testGetCartItemCountNoCart() {
            // Arrange
            when(cartRepository.findByUser(mockCustomer)).thenReturn(Optional.empty());

            // Act
            int result = cartService.getCartItemCount(mockCustomer);

            // Assert
            assertThat(result).isEqualTo(0);
            verify(cartRepository, times(1)).findByUser(mockCustomer);
        }
    }
}