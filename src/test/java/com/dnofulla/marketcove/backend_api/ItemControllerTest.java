package com.dnofulla.marketcove.backend_api;

import com.dnofulla.marketcove.backend_api.controller.ItemController;
import com.dnofulla.marketcove.backend_api.dto.item.CreateItemRequest;
import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import com.dnofulla.marketcove.backend_api.dto.item.UpdateItemRequest;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test class for ItemController endpoints
 */
@WebMvcTest(controllers = { ItemController.class })
@DisplayName("ItemController Tests")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class ItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ItemService itemService;

        @MockBean
        private com.dnofulla.marketcove.backend_api.util.JwtUtil jwtUtil;

        @MockBean
        private com.dnofulla.marketcove.backend_api.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        private User mockUser;
        private CreateItemRequest validCreateRequest;
        private UpdateItemRequest validUpdateRequest;
        private ItemResponse mockItemResponse;
        private List<ItemResponse> mockItemList;
        private Page<ItemResponse> mockItemPage;

        @BeforeEach
        void setUp() {
                // Setup mock user
                mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("seller@example.com");
                mockUser.setFirstName("Jane");
                mockUser.setLastName("Smith");
                mockUser.setRole(UserRole.SELLER);

                // Setup security context
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication())
                                .thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null,
                                                mockUser.getAuthorities()));
                SecurityContextHolder.setContext(securityContext);

                // Setup valid create request
                validCreateRequest = new CreateItemRequest();
                validCreateRequest.setItemName("Wireless Bluetooth Headphones");
                validCreateRequest.setItemDescription("High-quality wireless headphones with noise cancellation");
                validCreateRequest.setSku("WBH-001");
                validCreateRequest.setPrice(new BigDecimal("149.99"));
                validCreateRequest.setCompareAtPrice(new BigDecimal("199.99"));
                validCreateRequest.setStockQuantity(50);
                validCreateRequest.setLowStockThreshold(10);
                validCreateRequest.setWeight(new BigDecimal("0.3"));
                validCreateRequest.setWeightUnit("kg");
                validCreateRequest.setCategory("Electronics");
                validCreateRequest.setTags("wireless, bluetooth, headphones, audio");
                validCreateRequest.setRequiresShipping(true);
                validCreateRequest.setIsDigital(false);
                validCreateRequest.setSeoTitle("Best Wireless Bluetooth Headphones");
                validCreateRequest.setSeoDescription("Premium wireless headphones with superior sound quality");
                validCreateRequest.setImageUrls(Arrays.asList());

                // Setup valid update request
                validUpdateRequest = new UpdateItemRequest();
                validUpdateRequest.setItemName("Updated Wireless Bluetooth Headphones");
                validUpdateRequest.setItemDescription("Updated description");
                validUpdateRequest.setPrice(new BigDecimal("139.99"));

                // Setup mock item response
                mockItemResponse = new ItemResponse();
                mockItemResponse.setId(1L);
                mockItemResponse.setItemName("Wireless Bluetooth Headphones");
                mockItemResponse.setItemDescription("High-quality wireless headphones with noise cancellation");
                mockItemResponse.setSku("WBH-001");
                mockItemResponse.setPrice(new BigDecimal("149.99"));
                mockItemResponse.setCompareAtPrice(new BigDecimal("199.99"));
                mockItemResponse.setStockQuantity(50);
                mockItemResponse.setLowStockThreshold(10);
                mockItemResponse.setWeight(new BigDecimal("0.3"));
                mockItemResponse.setWeightUnit("kg");
                mockItemResponse.setCategory("Electronics");
                mockItemResponse.setTags("wireless, bluetooth, headphones, audio");
                mockItemResponse.setActive(true);
                mockItemResponse.setFeatured(false);
                mockItemResponse.setRequiresShipping(true);
                mockItemResponse.setDigital(false);
                mockItemResponse.setAverageRating(4.5);
                mockItemResponse.setTotalReviews(15);
                mockItemResponse.setTotalSales(25);
                mockItemResponse.setViewCount(150);
                mockItemResponse.setStorefrontId(1L);
                mockItemResponse.setStorefrontName("Tech Paradise");
                mockItemResponse.setOnSale(true);
                mockItemResponse.setDiscountAmount(new BigDecimal("50.00"));
                mockItemResponse.setDiscountPercentage(new BigDecimal("25.00"));
                mockItemResponse.setLowStock(false);
                mockItemResponse.setOutOfStock(false);
                mockItemResponse.setCreatedAt(LocalDateTime.now());
                mockItemResponse.setUpdatedAt(LocalDateTime.now());

                // Setup mock lists and pages
                mockItemList = Arrays.asList(mockItemResponse);
                mockItemPage = new PageImpl<>(mockItemList, PageRequest.of(0, 20), 1);
        }

        @Nested
        @DisplayName("Create Item Endpoint Tests")
        class CreateItemEndpointTests {

                @Test
                @DisplayName("Should create item successfully")
                void testCreateItemSuccess() throws Exception {
                        when(itemService.createItem(eq(1L), any(CreateItemRequest.class), any(User.class)))
                                        .thenReturn(mockItemResponse);

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.itemName").value("Wireless Bluetooth Headphones"))
                                        .andExpect(jsonPath("$.sku").value("WBH-001"))
                                        .andExpect(jsonPath("$.price").value(149.99))
                                        .andExpect(jsonPath("$.category").value("Electronics"))
                                        .andExpect(jsonPath("$.active").value(true));

                        verify(itemService, times(1)).createItem(eq(1L), any(CreateItemRequest.class), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when item name is missing")
                void testCreateItemMissingName() throws Exception {
                        validCreateRequest.setItemName("");

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when price is null")
                void testCreateItemNullPrice() throws Exception {
                        validCreateRequest.setPrice(null);

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when price is zero or negative")
                void testCreateItemInvalidPrice() throws Exception {
                        validCreateRequest.setPrice(new BigDecimal("0"));

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when stock quantity is negative")
                void testCreateItemNegativeStock() throws Exception {
                        validCreateRequest.setStockQuantity(-1);

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when service throws exception")
                void testCreateItemServiceException() throws Exception {
                        when(itemService.createItem(eq(1L), any(CreateItemRequest.class), any(User.class)))
                                        .thenThrow(new RuntimeException("Storefront not found with id: 1"));

                        mockMvc.perform(post("/api/items/storefront/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Storefront not found with id: 1"));
                }
        }

        @Nested
        @DisplayName("Update Item Endpoint Tests")
        class UpdateItemEndpointTests {

                @Test
                @DisplayName("Should update item successfully")
                void testUpdateItemSuccess() throws Exception {
                        ItemResponse updatedResponse = new ItemResponse();
                        updatedResponse.setId(1L);
                        updatedResponse.setItemName("Updated Wireless Bluetooth Headphones");
                        updatedResponse.setPrice(new BigDecimal("139.99"));

                        when(itemService.updateItem(eq(1L), any(UpdateItemRequest.class), any(User.class)))
                                        .thenReturn(updatedResponse);

                        mockMvc.perform(put("/api/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.itemName")
                                                        .value("Updated Wireless Bluetooth Headphones"))
                                        .andExpect(jsonPath("$.price").value(139.99));

                        verify(itemService, times(1)).updateItem(eq(1L), any(UpdateItemRequest.class), any(User.class));
                }

                @Test
                @DisplayName("Should return 403 when user doesn't own item")
                void testUpdateItemNotOwner() throws Exception {
                        when(itemService.updateItem(eq(1L), any(UpdateItemRequest.class), any(User.class)))
                                        .thenThrow(new RuntimeException("Access denied. You do not own this item."));

                        mockMvc.perform(put("/api/items/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Access denied. You do not own this item."));
                }

                @Test
                @DisplayName("Should return 404 when item not found")
                void testUpdateItemNotFound() throws Exception {
                        when(itemService.updateItem(eq(999L), any(UpdateItemRequest.class), any(User.class)))
                                        .thenThrow(new RuntimeException("Item not found with id: 999"));

                        mockMvc.perform(put("/api/items/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Item not found with id: 999"));
                }
        }

        @Nested
        @DisplayName("Get Item Endpoint Tests")
        class GetItemEndpointTests {

                @Test
                @DisplayName("Should get item by ID successfully")
                void testGetItemByIdSuccess() throws Exception {
                        when(itemService.getItem(1L)).thenReturn(mockItemResponse);

                        mockMvc.perform(get("/api/items/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.itemName").value("Wireless Bluetooth Headphones"))
                                        .andExpect(jsonPath("$.sku").value("WBH-001"))
                                        .andExpect(jsonPath("$.price").value(149.99));

                        verify(itemService, times(1)).getItem(1L);
                }

                @Test
                @DisplayName("Should get item by SKU successfully")
                void testGetItemBySkuSuccess() throws Exception {
                        when(itemService.getItemBySku("WBH-001")).thenReturn(mockItemResponse);

                        mockMvc.perform(get("/api/items/sku/WBH-001"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.itemName").value("Wireless Bluetooth Headphones"))
                                        .andExpect(jsonPath("$.sku").value("WBH-001"));

                        verify(itemService, times(1)).getItemBySku("WBH-001");
                }

                @Test
                @DisplayName("Should return 404 when item not found by ID")
                void testGetItemByIdNotFound() throws Exception {
                        when(itemService.getItem(999L))
                                        .thenThrow(new RuntimeException("Item not found with id: 999"));

                        mockMvc.perform(get("/api/items/999"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Item not found with id: 999"));
                }

                @Test
                @DisplayName("Should return 404 when item not found by SKU")
                void testGetItemBySkuNotFound() throws Exception {
                        when(itemService.getItemBySku("NONEXISTENT"))
                                        .thenThrow(new RuntimeException("Item not found with SKU: NONEXISTENT"));

                        mockMvc.perform(get("/api/items/sku/NONEXISTENT"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Item not found with SKU: NONEXISTENT"));
                }
        }

        @Nested
        @DisplayName("Get Storefront Items Endpoint Tests")
        class GetStorefrontItemsEndpointTests {

                @Test
                @DisplayName("Should get storefront items successfully")
                void testGetStorefrontItemsSuccess() throws Exception {
                        when(itemService.getStorefrontItems(eq(1L), eq(true), any(Pageable.class)))
                                        .thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/storefront/1")
                                        .param("activeOnly", "true")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content[0].id").value(1L))
                                        .andExpect(jsonPath("$.totalElements").value(1))
                                        .andExpect(jsonPath("$.totalPages").value(1));

                        verify(itemService, times(1)).getStorefrontItems(eq(1L), eq(true), any(Pageable.class));
                }

                @Test
                @DisplayName("Should get all storefront items including inactive")
                void testGetStorefrontItemsIncludeInactive() throws Exception {
                        when(itemService.getStorefrontItems(eq(1L), eq(false), any(Pageable.class)))
                                        .thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/storefront/1")
                                        .param("activeOnly", "false"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getStorefrontItems(eq(1L), eq(false), any(Pageable.class));
                }
        }

        @Nested
        @DisplayName("Get User Items Endpoint Tests")
        class GetUserItemsEndpointTests {

                @Test
                @DisplayName("Should get user's items successfully")
                void testGetUserItemsSuccess() throws Exception {
                        when(itemService.getUserItems(any(User.class))).thenReturn(mockItemList);

                        mockMvc.perform(get("/api/items/my-items"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$[0].id").value(1L))
                                        .andExpect(jsonPath("$[0].itemName").value("Wireless Bluetooth Headphones"));

                        verify(itemService, times(1)).getUserItems(any(User.class));
                }

                @Test
                @DisplayName("Should return empty list when user has no items")
                void testGetUserItemsEmpty() throws Exception {
                        when(itemService.getUserItems(any(User.class))).thenReturn(Arrays.asList());

                        mockMvc.perform(get("/api/items/my-items"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$").isEmpty());
                }
        }

        @Nested
        @DisplayName("Search and Filter Endpoint Tests")
        class SearchAndFilterEndpointTests {

                @Test
                @DisplayName("Should search items successfully")
                void testSearchItemsSuccess() throws Exception {
                        when(itemService.searchItems(eq("headphones"), any(Pageable.class))).thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/search")
                                        .param("query", "headphones")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content[0].itemName")
                                                        .value("Wireless Bluetooth Headphones"));

                        verify(itemService, times(1)).searchItems(eq("headphones"), any(Pageable.class));
                }

                @Test
                @DisplayName("Should get items by category successfully")
                void testGetItemsByCategorySuccess() throws Exception {
                        when(itemService.getItemsByCategory(eq("Electronics"), any(Pageable.class)))
                                        .thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/category/Electronics")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content[0].category").value("Electronics"));

                        verify(itemService, times(1)).getItemsByCategory(eq("Electronics"), any(Pageable.class));
                }

                @Test
                @DisplayName("Should get items by price range successfully")
                void testGetItemsByPriceRangeSuccess() throws Exception {
                        when(itemService.getItemsByPriceRange(eq(new BigDecimal("100")), eq(new BigDecimal("200")),
                                        any(Pageable.class)))
                                        .thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/price-range")
                                        .param("minPrice", "100")
                                        .param("maxPrice", "200")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getItemsByPriceRange(eq(new BigDecimal("100")),
                                        eq(new BigDecimal("200")),
                                        any(Pageable.class));
                }

                @Test
                @DisplayName("Should get featured items successfully")
                void testGetFeaturedItemsSuccess() throws Exception {
                        when(itemService.getFeaturedItems(any(Pageable.class))).thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/featured")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getFeaturedItems(any(Pageable.class));
                }

                @Test
                @DisplayName("Should get items on sale successfully")
                void testGetItemsOnSaleSuccess() throws Exception {
                        when(itemService.getItemsOnSale(any(Pageable.class))).thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/on-sale")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getItemsOnSale(any(Pageable.class));
                }

                @Test
                @DisplayName("Should get best selling items successfully")
                void testGetBestSellingItemsSuccess() throws Exception {
                        when(itemService.getBestSellingItems(any(Pageable.class))).thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/best-selling")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getBestSellingItems(any(Pageable.class));
                }

                @Test
                @DisplayName("Should get recently added items successfully")
                void testGetRecentlyAddedItemsSuccess() throws Exception {
                        when(itemService.getRecentlyAddedItems(any(Pageable.class))).thenReturn(mockItemPage);

                        mockMvc.perform(get("/api/items/recent")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(itemService, times(1)).getRecentlyAddedItems(any(Pageable.class));
                }
        }

        @Nested
        @DisplayName("Owner-specific Endpoint Tests")
        class OwnerSpecificEndpointTests {

                @Test
                @DisplayName("Should get low stock items successfully")
                void testGetLowStockItemsSuccess() throws Exception {
                        when(itemService.getLowStockItems(any(User.class))).thenReturn(mockItemList);

                        mockMvc.perform(get("/api/items/low-stock"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$[0].id").value(1L));

                        verify(itemService, times(1)).getLowStockItems(any(User.class));
                }

                @Test
                @DisplayName("Should update stock quantity successfully")
                void testUpdateStockSuccess() throws Exception {
                        ItemResponse updatedResponse = new ItemResponse();
                        updatedResponse.setId(1L);
                        updatedResponse.setStockQuantity(25);

                        when(itemService.updateStock(eq(1L), eq(25), any(User.class)))
                                        .thenReturn(updatedResponse);

                        mockMvc.perform(patch("/api/items/1/stock")
                                        .param("quantity", "25"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.stockQuantity").value(25));

                        verify(itemService, times(1)).updateStock(eq(1L), eq(25), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when updating stock with negative quantity")
                void testUpdateStockNegativeQuantity() throws Exception {
                        when(itemService.updateStock(eq(1L), eq(-5), any(User.class)))
                                        .thenThrow(new RuntimeException("Stock quantity cannot be negative"));

                        mockMvc.perform(patch("/api/items/1/stock")
                                        .param("quantity", "-5"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Stock quantity cannot be negative"));
                }
        }

        @Nested
        @DisplayName("Delete Item Endpoint Tests")
        class DeleteItemEndpointTests {

                @Test
                @DisplayName("Should delete item successfully")
                void testDeleteItemSuccess() throws Exception {
                        doNothing().when(itemService).deleteItem(eq(1L), any(User.class));

                        mockMvc.perform(delete("/api/items/1"))
                                        .andExpect(status().isNoContent());

                        verify(itemService, times(1)).deleteItem(eq(1L), any(User.class));
                }

                @Test
                @DisplayName("Should return 403 when user doesn't own item")
                void testDeleteItemNotOwner() throws Exception {
                        doThrow(new RuntimeException("Access denied. You do not own this item."))
                                        .when(itemService).deleteItem(eq(1L), any(User.class));

                        mockMvc.perform(delete("/api/items/1"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Access denied. You do not own this item."));
                }

                @Test
                @DisplayName("Should return 404 when item not found")
                void testDeleteItemNotFound() throws Exception {
                        doThrow(new RuntimeException("Item not found with id: 999"))
                                        .when(itemService).deleteItem(eq(999L), any(User.class));

                        mockMvc.perform(delete("/api/items/999"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Item not found with id: 999"));
                }
        }

        @Nested
        @DisplayName("Image Management Endpoint Tests")
        class ImageManagementEndpointTests {

                @Test
                @DisplayName("Should upload item images successfully")
                void testUploadItemImagesSuccess() throws Exception {
                        MockMultipartFile imageFile1 = new MockMultipartFile(
                                        "files", "image1.jpg", "image/jpeg", "test image 1 content".getBytes());
                        MockMultipartFile imageFile2 = new MockMultipartFile(
                                        "files", "image2.jpg", "image/jpeg", "test image 2 content".getBytes());

                        ItemResponse responseWithImages = new ItemResponse();
                        responseWithImages.setId(1L);
                        responseWithImages.setItemName("Wireless Bluetooth Headphones");
                        responseWithImages
                                        .setImageUrls(Arrays.asList("https://example.com/image1.jpg",
                                                        "https://example.com/image2.jpg"));

                        when(itemService.uploadItemImages(eq(1L), anyList(), any(User.class)))
                                        .thenReturn(responseWithImages);

                        mockMvc.perform(multipart("/api/items/1/images")
                                        .file(imageFile1)
                                        .file(imageFile2))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.imageUrls").isArray())
                                        .andExpect(jsonPath("$.imageUrls").value(org.hamcrest.Matchers.hasSize(2)));

                        verify(itemService, times(1)).uploadItemImages(eq(1L), anyList(), any(User.class));
                }

                @Test
                @DisplayName("Should remove item image successfully")
                void testRemoveItemImageSuccess() throws Exception {
                        ItemResponse responseWithRemovedImage = new ItemResponse();
                        responseWithRemovedImage.setId(1L);
                        responseWithRemovedImage.setItemName("Wireless Bluetooth Headphones");
                        responseWithRemovedImage.setImageUrls(Arrays.asList("https://example.com/image2.jpg"));

                        when(itemService.removeItemImage(eq(1L), eq("https://example.com/image1.jpg"), any(User.class)))
                                        .thenReturn(responseWithRemovedImage);

                        mockMvc.perform(delete("/api/items/1/images")
                                        .param("imageUrl", "https://example.com/image1.jpg"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.imageUrls").isArray())
                                        .andExpect(jsonPath("$.imageUrls").value(org.hamcrest.Matchers.hasSize(1)));

                        verify(itemService, times(1)).removeItemImage(eq(1L), eq("https://example.com/image1.jpg"),
                                        any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when image upload fails")
                void testUploadItemImagesFailure() throws Exception {
                        MockMultipartFile imageFile = new MockMultipartFile(
                                        "files", "image.jpg", "image/jpeg", "test image content".getBytes());

                        when(itemService.uploadItemImages(eq(1L), anyList(), any(User.class)))
                                        .thenThrow(new RuntimeException("Failed to upload images"));

                        mockMvc.perform(multipart("/api/items/1/images")
                                        .file(imageFile))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Failed to upload images"));
                }

                @Test
                @DisplayName("Should return 400 when image removal fails")
                void testRemoveItemImageFailure() throws Exception {
                        when(itemService.removeItemImage(eq(1L), eq("https://example.com/nonexistent.jpg"),
                                        any(User.class)))
                                        .thenThrow(new RuntimeException("Image not found for this item"));

                        mockMvc.perform(delete("/api/items/1/images")
                                        .param("imageUrl", "https://example.com/nonexistent.jpg"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Image not found for this item"));
                }
        }
}
