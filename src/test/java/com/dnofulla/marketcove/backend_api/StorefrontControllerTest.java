package com.dnofulla.marketcove.backend_api;

import com.dnofulla.marketcove.backend_api.controller.StorefrontController;
import com.dnofulla.marketcove.backend_api.dto.storefront.CreateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.dto.storefront.StorefrontResponse;
import com.dnofulla.marketcove.backend_api.dto.storefront.UpdateStorefrontRequest;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.service.StorefrontService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test class for StorefrontController endpoints
 */
@WebMvcTest(controllers = { StorefrontController.class })
@DisplayName("StorefrontController Tests")
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class StorefrontControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private StorefrontService storefrontService;

        @MockBean
        private com.dnofulla.marketcove.backend_api.util.JwtUtil jwtUtil;

        @MockBean
        private com.dnofulla.marketcove.backend_api.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

        private User mockUser;
        private CreateStorefrontRequest validCreateRequest;
        private UpdateStorefrontRequest validUpdateRequest;
        private StorefrontResponse mockStorefrontResponse;
        private List<StorefrontResponse> mockStorefrontList;
        private Page<StorefrontResponse> mockStorefrontPage;

        @BeforeEach
        void setUp() {
                // Setup mock user
                mockUser = new User();
                mockUser.setId(1L);
                mockUser.setEmail("test@example.com");
                mockUser.setFirstName("John");
                mockUser.setLastName("Doe");
                mockUser.setRole(UserRole.BUSINESS_OWNER);

                // Setup security context
                SecurityContext securityContext = mock(SecurityContext.class);
                when(securityContext.getAuthentication())
                                .thenReturn(new UsernamePasswordAuthenticationToken(mockUser, null,
                                                mockUser.getAuthorities()));
                SecurityContextHolder.setContext(securityContext);

                // Setup valid create request
                validCreateRequest = new CreateStorefrontRequest();
                validCreateRequest.setStoreName("Tech Paradise");
                validCreateRequest.setStoreDescription("Your one-stop shop for technology");
                validCreateRequest.setStoreUrlSlug("tech-paradise");
                validCreateRequest.setContactEmail("info@techparadise.com");
                validCreateRequest.setContactPhone("+1-555-0123");
                validCreateRequest.setReturnPolicy("30-day return policy");
                validCreateRequest.setShippingPolicy("Free shipping on orders over $50");
                validCreateRequest.setWebsiteUrl("https://techparadise.com");
                validCreateRequest.setFacebookUrl("https://facebook.com/techparadise");

                // Setup valid update request
                validUpdateRequest = new UpdateStorefrontRequest();
                validUpdateRequest.setStoreName("Updated Tech Paradise");
                validUpdateRequest.setStoreDescription("Updated description");

                // Setup mock storefront response
                mockStorefrontResponse = new StorefrontResponse();
                mockStorefrontResponse.setId(1L);
                mockStorefrontResponse.setStoreName("Tech Paradise");
                mockStorefrontResponse.setStoreDescription("Your one-stop shop for technology");
                mockStorefrontResponse.setStoreUrlSlug("tech-paradise");
                mockStorefrontResponse.setContactEmail("info@techparadise.com");
                mockStorefrontResponse.setContactPhone("+1-555-0123");
                mockStorefrontResponse.setActive(true);
                mockStorefrontResponse.setFeatured(false);
                mockStorefrontResponse.setAverageRating(4.5);
                mockStorefrontResponse.setTotalReviews(10);
                mockStorefrontResponse.setTotalSales(1250.0);
                mockStorefrontResponse.setTotalOrders(25);
                mockStorefrontResponse.setTotalItems(50);
                mockStorefrontResponse.setOwnerName("John Doe");
                mockStorefrontResponse.setOwnerType("BUSINESS");
                mockStorefrontResponse.setCreatedAt(LocalDateTime.now());
                mockStorefrontResponse.setUpdatedAt(LocalDateTime.now());

                // Setup mock lists and pages
                mockStorefrontList = Arrays.asList(mockStorefrontResponse);
                mockStorefrontPage = new PageImpl<>(mockStorefrontList, PageRequest.of(0, 20), 1);
        }

        @Nested
        @DisplayName("Create Storefront Endpoint Tests")
        class CreateStorefrontEndpointTests {

                @Test
                @DisplayName("Should create storefront successfully")
                void testCreateStorefrontSuccess() throws Exception {
                        when(storefrontService.createStorefront(any(CreateStorefrontRequest.class), any(User.class)))
                                        .thenReturn(mockStorefrontResponse);

                        mockMvc.perform(post("/api/storefronts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeName").value("Tech Paradise"))
                                        .andExpect(jsonPath("$.storeUrlSlug").value("tech-paradise"))
                                        .andExpect(jsonPath("$.contactEmail").value("info@techparadise.com"))
                                        .andExpect(jsonPath("$.active").value(true));

                        verify(storefrontService, times(1)).createStorefront(any(CreateStorefrontRequest.class),
                                        any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when store name is missing")
                void testCreateStorefrontMissingName() throws Exception {
                        validCreateRequest.setStoreName("");

                        mockMvc.perform(post("/api/storefronts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when store name is too long")
                void testCreateStorefrontNameTooLong() throws Exception {
                        validCreateRequest.setStoreName("A".repeat(101));

                        mockMvc.perform(post("/api/storefronts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when contact email is invalid")
                void testCreateStorefrontInvalidEmail() throws Exception {
                        validCreateRequest.setContactEmail("invalid-email");

                        mockMvc.perform(post("/api/storefronts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("Should return 400 when service throws exception")
                void testCreateStorefrontServiceException() throws Exception {
                        when(storefrontService.createStorefront(any(CreateStorefrontRequest.class), any(User.class)))
                                        .thenThrow(new RuntimeException(
                                                        "Storefront with slug 'tech-paradise' already exists"));

                        mockMvc.perform(post("/api/storefronts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Storefront with slug 'tech-paradise' already exists"));
                }
        }

        @Nested
        @DisplayName("Update Storefront Endpoint Tests")
        class UpdateStorefrontEndpointTests {

                @Test
                @DisplayName("Should update storefront successfully")
                void testUpdateStorefrontSuccess() throws Exception {
                        StorefrontResponse updatedResponse = new StorefrontResponse();
                        updatedResponse.setId(1L);
                        updatedResponse.setStoreName("Updated Tech Paradise");
                        updatedResponse.setStoreDescription("Updated description");

                        when(storefrontService.updateStorefront(eq(1L), any(UpdateStorefrontRequest.class),
                                        any(User.class)))
                                        .thenReturn(updatedResponse);

                        mockMvc.perform(put("/api/storefronts/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeName").value("Updated Tech Paradise"))
                                        .andExpect(jsonPath("$.storeDescription").value("Updated description"));

                        verify(storefrontService, times(1)).updateStorefront(eq(1L), any(UpdateStorefrontRequest.class),
                                        any(User.class));
                }

                @Test
                @DisplayName("Should return 403 when user doesn't own storefront")
                void testUpdateStorefrontNotOwner() throws Exception {
                        when(storefrontService.updateStorefront(eq(1L), any(UpdateStorefrontRequest.class),
                                        any(User.class)))
                                        .thenThrow(new RuntimeException(
                                                        "Access denied. You do not own this storefront."));

                        mockMvc.perform(put("/api/storefronts/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Access denied. You do not own this storefront."));
                }

                @Test
                @DisplayName("Should return 404 when storefront not found")
                void testUpdateStorefrontNotFound() throws Exception {
                        when(storefrontService.updateStorefront(eq(999L), any(UpdateStorefrontRequest.class),
                                        any(User.class)))
                                        .thenThrow(new RuntimeException("Storefront not found with id: 999"));

                        mockMvc.perform(put("/api/storefronts/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Storefront not found with id: 999"));
                }
        }

        @Nested
        @DisplayName("Get Storefront Endpoint Tests")
        class GetStorefrontEndpointTests {

                @Test
                @DisplayName("Should get storefront by ID successfully")
                void testGetStorefrontByIdSuccess() throws Exception {
                        when(storefrontService.getStorefront(1L)).thenReturn(mockStorefrontResponse);

                        mockMvc.perform(get("/api/storefronts/1"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeName").value("Tech Paradise"))
                                        .andExpect(jsonPath("$.storeUrlSlug").value("tech-paradise"));

                        verify(storefrontService, times(1)).getStorefront(1L);
                }

                @Test
                @DisplayName("Should get storefront by slug successfully")
                void testGetStorefrontBySlugSuccess() throws Exception {
                        when(storefrontService.getStorefrontBySlug("tech-paradise")).thenReturn(mockStorefrontResponse);

                        mockMvc.perform(get("/api/storefronts/slug/tech-paradise"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeName").value("Tech Paradise"))
                                        .andExpect(jsonPath("$.storeUrlSlug").value("tech-paradise"));

                        verify(storefrontService, times(1)).getStorefrontBySlug("tech-paradise");
                }

                @Test
                @DisplayName("Should return 404 when storefront not found by ID")
                void testGetStorefrontByIdNotFound() throws Exception {
                        when(storefrontService.getStorefront(999L))
                                        .thenThrow(new RuntimeException("Storefront not found with id: 999"));

                        mockMvc.perform(get("/api/storefronts/999"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Storefront not found with id: 999"));
                }

                @Test
                @DisplayName("Should return 404 when storefront not found by slug")
                void testGetStorefrontBySlugNotFound() throws Exception {
                        when(storefrontService.getStorefrontBySlug("nonexistent"))
                                        .thenThrow(new RuntimeException("Storefront not found with slug: nonexistent"));

                        mockMvc.perform(get("/api/storefronts/slug/nonexistent"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Storefront not found with slug: nonexistent"));
                }
        }

        @Nested
        @DisplayName("Get User Storefronts Endpoint Tests")
        class GetUserStorefrontsEndpointTests {

                @Test
                @DisplayName("Should get user's storefronts successfully")
                void testGetUserStorefrontsSuccess() throws Exception {
                        when(storefrontService.getUserStorefronts(any(User.class))).thenReturn(mockStorefrontList);

                        mockMvc.perform(get("/api/storefronts/my-storefronts"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$[0].id").value(1L))
                                        .andExpect(jsonPath("$[0].storeName").value("Tech Paradise"));

                        verify(storefrontService, times(1)).getUserStorefronts(any(User.class));
                }

                @Test
                @DisplayName("Should return empty list when user has no storefronts")
                void testGetUserStorefrontsEmpty() throws Exception {
                        when(storefrontService.getUserStorefronts(any(User.class))).thenReturn(Arrays.asList());

                        mockMvc.perform(get("/api/storefronts/my-storefronts"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").isArray())
                                        .andExpect(jsonPath("$").isEmpty());
                }
        }

        @Nested
        @DisplayName("Public Storefront Listing Endpoint Tests")
        class PublicStorefrontListingEndpointTests {

                @Test
                @DisplayName("Should get active storefronts with pagination")
                void testGetActiveStorefrontsSuccess() throws Exception {
                        when(storefrontService.getActiveStorefronts(any(Pageable.class)))
                                        .thenReturn(mockStorefrontPage);

                        mockMvc.perform(get("/api/storefronts")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content[0].id").value(1L))
                                        .andExpect(jsonPath("$.totalElements").value(1))
                                        .andExpect(jsonPath("$.totalPages").value(1));

                        verify(storefrontService, times(1)).getActiveStorefronts(any(Pageable.class));
                }

                @Test
                @DisplayName("Should search storefronts successfully")
                void testSearchStorefrontsSuccess() throws Exception {
                        when(storefrontService.searchStorefronts(eq("tech"), any(Pageable.class)))
                                        .thenReturn(mockStorefrontPage);

                        mockMvc.perform(get("/api/storefronts/search")
                                        .param("query", "tech")
                                        .param("page", "0")
                                        .param("size", "20"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray())
                                        .andExpect(jsonPath("$.content[0].storeName").value("Tech Paradise"));

                        verify(storefrontService, times(1)).searchStorefronts(eq("tech"), any(Pageable.class));
                }

                @Test
                @DisplayName("Should get featured storefronts successfully")
                void testGetFeaturedStorefrontsSuccess() throws Exception {
                        when(storefrontService.getFeaturedStorefronts(any(Pageable.class)))
                                        .thenReturn(mockStorefrontPage);

                        mockMvc.perform(get("/api/storefronts/featured")
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(storefrontService, times(1)).getFeaturedStorefronts(any(Pageable.class));
                }

                @Test
                @DisplayName("Should get top rated storefronts successfully")
                void testGetTopRatedStorefrontsSuccess() throws Exception {
                        when(storefrontService.getTopRatedStorefronts(any(Pageable.class)))
                                        .thenReturn(mockStorefrontPage);

                        mockMvc.perform(get("/api/storefronts/top-rated")
                                        .param("page", "0")
                                        .param("size", "10"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.content").isArray());

                        verify(storefrontService, times(1)).getTopRatedStorefronts(any(Pageable.class));
                }
        }

        @Nested
        @DisplayName("Delete Storefront Endpoint Tests")
        class DeleteStorefrontEndpointTests {

                @Test
                @DisplayName("Should delete storefront successfully")
                void testDeleteStorefrontSuccess() throws Exception {
                        doNothing().when(storefrontService).deleteStorefront(eq(1L), any(User.class));

                        mockMvc.perform(delete("/api/storefronts/1"))
                                        .andExpect(status().isNoContent());

                        verify(storefrontService, times(1)).deleteStorefront(eq(1L), any(User.class));
                }

                @Test
                @DisplayName("Should return 403 when user doesn't own storefront")
                void testDeleteStorefrontNotOwner() throws Exception {
                        doThrow(new RuntimeException("Access denied. You do not own this storefront."))
                                        .when(storefrontService).deleteStorefront(eq(1L), any(User.class));

                        mockMvc.perform(delete("/api/storefronts/1"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Access denied. You do not own this storefront."));
                }

                @Test
                @DisplayName("Should return 404 when storefront not found")
                void testDeleteStorefrontNotFound() throws Exception {
                        doThrow(new RuntimeException("Storefront not found with id: 999"))
                                        .when(storefrontService).deleteStorefront(eq(999L), any(User.class));

                        mockMvc.perform(delete("/api/storefronts/999"))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Storefront not found with id: 999"));
                }
        }

        @Nested
        @DisplayName("Image Upload Endpoint Tests")
        class ImageUploadEndpointTests {

                @Test
                @DisplayName("Should upload storefront logo successfully")
                void testUploadStorefrontLogoSuccess() throws Exception {
                        MockMultipartFile logoFile = new MockMultipartFile(
                                        "file", "logo.jpg", "image/jpeg", "test image content".getBytes());

                        StorefrontResponse responseWithLogo = new StorefrontResponse();
                        responseWithLogo.setId(1L);
                        responseWithLogo.setStoreName("Tech Paradise");
                        responseWithLogo.setStoreLogoUrl("https://example.com/logo.jpg");

                        when(storefrontService.uploadStorefrontLogo(eq(1L), any(), any(User.class)))
                                        .thenReturn(responseWithLogo);

                        mockMvc.perform(multipart("/api/storefronts/1/logo")
                                        .file(logoFile))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeLogoUrl").value("https://example.com/logo.jpg"));

                        verify(storefrontService, times(1)).uploadStorefrontLogo(eq(1L), any(), any(User.class));
                }

                @Test
                @DisplayName("Should upload storefront banner successfully")
                void testUploadStorefrontBannerSuccess() throws Exception {
                        MockMultipartFile bannerFile = new MockMultipartFile(
                                        "file", "banner.jpg", "image/jpeg", "test banner content".getBytes());

                        StorefrontResponse responseWithBanner = new StorefrontResponse();
                        responseWithBanner.setId(1L);
                        responseWithBanner.setStoreName("Tech Paradise");
                        responseWithBanner.setStoreBannerUrl("https://example.com/banner.jpg");

                        when(storefrontService.uploadStorefrontBanner(eq(1L), any(), any(User.class)))
                                        .thenReturn(responseWithBanner);

                        mockMvc.perform(multipart("/api/storefronts/1/banner")
                                        .file(bannerFile))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(1L))
                                        .andExpect(jsonPath("$.storeBannerUrl")
                                                        .value("https://example.com/banner.jpg"));

                        verify(storefrontService, times(1)).uploadStorefrontBanner(eq(1L), any(), any(User.class));
                }

                @Test
                @DisplayName("Should return 400 when logo upload fails")
                void testUploadStorefrontLogoFailure() throws Exception {
                        MockMultipartFile logoFile = new MockMultipartFile(
                                        "file", "logo.jpg", "image/jpeg", "test image content".getBytes());

                        when(storefrontService.uploadStorefrontLogo(eq(1L), any(), any(User.class)))
                                        .thenThrow(new RuntimeException("Failed to upload image"));

                        mockMvc.perform(multipart("/api/storefronts/1/logo")
                                        .file(logoFile))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message").value("Failed to upload image"));
                }

                @Test
                @DisplayName("Should return 400 when banner upload fails")
                void testUploadStorefrontBannerFailure() throws Exception {
                        MockMultipartFile bannerFile = new MockMultipartFile(
                                        "file", "banner.jpg", "image/jpeg", "test banner content".getBytes());

                        when(storefrontService.uploadStorefrontBanner(eq(1L), any(), any(User.class)))
                                        .thenThrow(new RuntimeException(
                                                        "Access denied. You do not own this storefront."));

                        mockMvc.perform(multipart("/api/storefronts/1/banner")
                                        .file(bannerFile))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(jsonPath("$.message")
                                                        .value("Access denied. You do not own this storefront."));
                }
        }
}