package com.dnofulla.marketcove.backend_api;

import com.dnofulla.marketcove.backend_api.controller.CartController;
import com.dnofulla.marketcove.backend_api.dto.cart.AddToCartRequest;
import com.dnofulla.marketcove.backend_api.dto.cart.CartItemResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.CartResponse;
import com.dnofulla.marketcove.backend_api.dto.cart.UpdateCartItemRequest;
import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test class for CartController endpoints
 */
@WebMvcTest(controllers = { CartController.class })
@DisplayName("CartController Tests")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CartService cartService;

        @MockBean
        private com.dnofulla.marketcove.backend_api.util.JwtUtil jwtUtil;

        @MockBean
        private com.dnofulla.marketcove.backend_api.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        private User mockCustomer;
        private AddToCartRequest validAddToCartRequest;
        private UpdateCartItemRequest validUpdateRequest;
        private CartResponse mockCartResponse;
        private CartItemResponse mockCartItemResponse;
        private ItemResponse mockItemResponse;

        @BeforeEach
        void setUp() {
                // Setup mock customer user
                mockCustomer = new User();
                mockCustomer.setId(1L);
                mockCustomer.setEmail("customer@test.com");
                mockCustomer.setFirstName("John");
                mockCustomer.setLastName("Doe");
                mockCustomer.setRole(UserRole.CUSTOMER);

                // Setup security context
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication())
                                .thenReturn(new UsernamePasswordAuthenticationToken(mockCustomer, null,
                                                mockCustomer.getAuthorities()));
                SecurityContextHolder.setContext(securityContext);

                // Setup valid add to cart request
                validAddToCartRequest = new AddToCartRequest();
                validAddToCartRequest.setItemId(1L);
                validAddToCartRequest.setQuantity(2);

                // Setup valid update request
                validUpdateRequest = new UpdateCartItemRequest();
                validUpdateRequest.setQuantity(3);

                // Setup mock item response
                mockItemResponse = new ItemResponse();
                mockItemResponse.setId(1L);
                mockItemResponse.setItemName("Test Item");
                mockItemResponse.setPrice(BigDecimal.valueOf(19.99));
                mockItemResponse.setActive(true);
                mockItemResponse.setStockQuantity(10);

                // Setup mock cart item response
                mockCartItemResponse = CartItemResponse.builder()
                                .id(1L)
                                .item(mockItemResponse)
                                .quantity(2)
                                .priceAtTime(BigDecimal.valueOf(19.99))
                                .subtotal(BigDecimal.valueOf(39.98))
                                .priceChanged(false)
                                .currentItemPrice(BigDecimal.valueOf(19.99))
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Setup mock cart response
                List<CartItemResponse> cartItems = new ArrayList<>();
                cartItems.add(mockCartItemResponse);

                mockCartResponse = CartResponse.builder()
                                .id(1L)
                                .userId(1L)
                                .cartItems(cartItems)
                                .totalAmount(BigDecimal.valueOf(39.98))
                                .totalItems(2)
                                .empty(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        @Nested
        @DisplayName("Get Cart Endpoint Tests")
        class GetCartEndpointTests {

                @Test
                @DisplayName("Should get cart successfully for authenticated customer")
                void testGetCartSuccess() throws Exception {
                        when(cartService.getCart(any(User.class))).thenReturn(mockCartResponse);

                        mockMvc.perform(get("/api/cart"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.userId").value(1L))
                                        .andExpect(jsonPath("$.totalAmount").value(39.98))
                                        .andExpect(jsonPath("$.totalItems").value(2))
                                        .andExpect(jsonPath("$.empty").value(false))
                                        .andExpect(jsonPath("$.cartItems").isArray())
                                        .andExpect(jsonPath("$.cartItems[0].item.itemName").value("Test Item"));

                        verify(cartService, times(1)).getCart(any(User.class));
                }

                @Test
                @DisplayName("Should get empty cart for new customer")
                void testGetEmptyCart() throws Exception {
                        CartResponse emptyCart = CartResponse.builder()
                                        .id(1L)
                                        .userId(1L)
                                        .cartItems(new ArrayList<>())
                                        .totalAmount(BigDecimal.ZERO)
                                        .totalItems(0)
                                        .empty(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        when(cartService.getCart(any(User.class))).thenReturn(emptyCart);

                        mockMvc.perform(get("/api/cart"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.empty").value(true))
                                        .andExpect(jsonPath("$.totalItems").value(0))
                                        .andExpect(jsonPath("$.totalAmount").value(0))
                                        .andExpect(jsonPath("$.cartItems").isEmpty());

                        verify(cartService, times(1)).getCart(any(User.class));
                }

                @Test
                @DisplayName("Should return 500 on unexpected error")
                void testGetCartUnexpectedError() throws Exception {
                        when(cartService.getCart(any(User.class)))
                                        .thenThrow(new RuntimeException("Database connection failed"));

                        mockMvc.perform(get("/api/cart"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Add to Cart Endpoint Tests")
        class AddToCartEndpointTests {

                @Test
                @DisplayName("Should add item to cart successfully")
                void testAddToCartSuccess() throws Exception {
                        when(cartService.addToCart(any(AddToCartRequest.class), any(User.class)))
                                        .thenReturn(mockCartResponse);

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.totalItems").value(2))
                                        .andExpect(jsonPath("$.cartItems[0].quantity").value(2))
                                        .andExpect(jsonPath("$.cartItems[0].item.id").value(1L));

                        verify(cartService, times(1)).addToCart(any(AddToCartRequest.class), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when item ID is missing")
                void testAddToCartMissingItemId() throws Exception {
                        validAddToCartRequest.setItemId(null);

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when quantity is missing")
                void testAddToCartMissingQuantity() throws Exception {
                        validAddToCartRequest.setQuantity(null);

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when quantity is less than 1")
                void testAddToCartInvalidQuantity() throws Exception {
                        validAddToCartRequest.setQuantity(0);

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when item not found")
                void testAddToCartItemNotFound() throws Exception {
                        when(cartService.addToCart(any(AddToCartRequest.class), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Item not found with ID: 1"));

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Item not found with ID: 1"));
                }

                @Test
                @DisplayName("Should return 400 when insufficient stock")
                void testAddToCartInsufficientStock() throws Exception {
                        when(cartService.addToCart(any(AddToCartRequest.class), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Insufficient stock. Available: 1"));

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Insufficient stock. Available: 1"));
                }

                @Test
                @DisplayName("Should return 400 on unexpected error")
                void testAddToCartUnexpectedError() throws Exception {
                        when(cartService.addToCart(any(AddToCartRequest.class), any(User.class)))
                                        .thenThrow(new RuntimeException("Database connection failed"));

                        mockMvc.perform(post("/api/cart/add")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validAddToCartRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Update Cart Item Endpoint Tests")
        class UpdateCartItemEndpointTests {

                @Test
                @DisplayName("Should update cart item quantity successfully")
                void testUpdateCartItemSuccess() throws Exception {
                        mockCartItemResponse.setQuantity(3);
                        mockCartItemResponse.setSubtotal(BigDecimal.valueOf(59.97));
                        mockCartResponse.setTotalItems(3);
                        mockCartResponse.setTotalAmount(BigDecimal.valueOf(59.97));

                        when(cartService.updateCartItem(eq(1L), any(UpdateCartItemRequest.class), any(User.class)))
                                        .thenReturn(mockCartResponse);

                        mockMvc.perform(put("/api/cart/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.totalItems").value(3))
                                        .andExpect(jsonPath("$.cartItems[0].quantity").value(3));

                        verify(cartService, times(1))
                                        .updateCartItem(eq(1L), any(UpdateCartItemRequest.class), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when quantity is missing")
                void testUpdateCartItemMissingQuantity() throws Exception {
                        validUpdateRequest.setQuantity(null);

                        mockMvc.perform(put("/api/cart/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when quantity is less than 1")
                void testUpdateCartItemInvalidQuantity() throws Exception {
                        validUpdateRequest.setQuantity(0);

                        mockMvc.perform(put("/api/cart/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when cart item not found")
                void testUpdateCartItemNotFound() throws Exception {
                        when(cartService.updateCartItem(eq(999L), any(UpdateCartItemRequest.class), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Cart item not found with ID: 999"));

                        mockMvc.perform(put("/api/cart/items/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Cart item not found with ID: 999"));
                }

                @Test
                @DisplayName("Should return 400 when insufficient stock")
                void testUpdateCartItemInsufficientStock() throws Exception {
                        when(cartService.updateCartItem(eq(1L), any(UpdateCartItemRequest.class), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Insufficient stock. Available: 2"));

                        mockMvc.perform(put("/api/cart/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Insufficient stock. Available: 2"));
                }

                @Test
                @DisplayName("Should return 400 when unauthorized access")
                void testUpdateCartItemUnauthorized() throws Exception {
                        when(cartService.updateCartItem(eq(1L), any(UpdateCartItemRequest.class), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Unauthorized access to cart item"));

                        mockMvc.perform(put("/api/cart/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Unauthorized access to cart item"));
                }
        }

        @Nested
        @DisplayName("Remove from Cart Endpoint Tests")
        class RemoveFromCartEndpointTests {

                @Test
                @DisplayName("Should remove item from cart successfully")
                void testRemoveFromCartSuccess() throws Exception {
                        CartResponse emptyCart = CartResponse.builder()
                                        .id(1L)
                                        .userId(1L)
                                        .cartItems(new ArrayList<>())
                                        .totalAmount(BigDecimal.ZERO)
                                        .totalItems(0)
                                        .empty(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        when(cartService.removeFromCart(eq(1L), any(User.class))).thenReturn(emptyCart);

                        mockMvc.perform(delete("/api/cart/items/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.empty").value(true))
                                        .andExpect(jsonPath("$.totalItems").value(0))
                                        .andExpect(jsonPath("$.cartItems").isEmpty());

                        verify(cartService, times(1)).removeFromCart(eq(1L), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when cart item not found")
                void testRemoveFromCartItemNotFound() throws Exception {
                        when(cartService.removeFromCart(eq(999L), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Cart item not found with ID: 999"));

                        mockMvc.perform(delete("/api/cart/items/999"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Cart item not found with ID: 999"));
                }

                @Test
                @DisplayName("Should return 400 when unauthorized access")
                void testRemoveFromCartUnauthorized() throws Exception {
                        when(cartService.removeFromCart(eq(1L), any(User.class)))
                                        .thenThrow(new IllegalArgumentException("Unauthorized access to cart item"));

                        mockMvc.perform(delete("/api/cart/items/1"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Unauthorized access to cart item"));
                }
        }

        @Nested
        @DisplayName("Clear Cart Endpoint Tests")
        class ClearCartEndpointTests {

                @Test
                @DisplayName("Should clear cart successfully")
                void testClearCartSuccess() throws Exception {
                        CartResponse emptyCart = CartResponse.builder()
                                        .id(1L)
                                        .userId(1L)
                                        .cartItems(new ArrayList<>())
                                        .totalAmount(BigDecimal.ZERO)
                                        .totalItems(0)
                                        .empty(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        when(cartService.clearCart(any(User.class))).thenReturn(emptyCart);

                        mockMvc.perform(delete("/api/cart/clear"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.empty").value(true))
                                        .andExpect(jsonPath("$.totalItems").value(0))
                                        .andExpect(jsonPath("$.cartItems").isEmpty());

                        verify(cartService, times(1)).clearCart(any(User.class));
                }

                @Test
                @DisplayName("Should return 400 on unexpected error")
                void testClearCartUnexpectedError() throws Exception {
                        when(cartService.clearCart(any(User.class)))
                                        .thenThrow(new RuntimeException("Database connection failed"));

                        mockMvc.perform(delete("/api/cart/clear"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Database connection failed"));
                }
        }

        @Nested
        @DisplayName("Get Cart Item Count Endpoint Tests")
        class GetCartItemCountEndpointTests {

                @Test
                @DisplayName("Should get cart item count successfully")
                void testGetCartItemCountSuccess() throws Exception {
                        when(cartService.getCartItemCount(any(User.class))).thenReturn(5);

                        mockMvc.perform(get("/api/cart/count"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(content().string("5"));

                        verify(cartService, times(1)).getCartItemCount(any(User.class));
                }

                @Test
                @DisplayName("Should return 0 for empty cart")
                void testGetCartItemCountEmpty() throws Exception {
                        when(cartService.getCartItemCount(any(User.class))).thenReturn(0);

                        mockMvc.perform(get("/api/cart/count"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string("0"));

                        verify(cartService, times(1)).getCartItemCount(any(User.class));
                }

                @Test
                @DisplayName("Should return 400 on unexpected error")
                void testGetCartItemCountUnexpectedError() throws Exception {
                        when(cartService.getCartItemCount(any(User.class)))
                                        .thenThrow(new RuntimeException("Database connection failed"));

                        mockMvc.perform(get("/api/cart/count"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Database connection failed"));
                }
        }
}