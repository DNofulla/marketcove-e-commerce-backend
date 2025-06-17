package com.dnofulla.marketcove.backend_api.service;

import com.dnofulla.marketcove.backend_api.dto.auth.*;
import com.dnofulla.marketcove.backend_api.entity.BusinessProfile;
import com.dnofulla.marketcove.backend_api.entity.SellerProfile;
import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.repository.BusinessProfileRepository;
import com.dnofulla.marketcove.backend_api.repository.SellerProfileRepository;
import com.dnofulla.marketcove.backend_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling authentication operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserService userService;
    private final BusinessProfileRepository businessProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate request
        validateRegisterRequest(request);

        // Create user entity
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());

        // Create user
        User savedUser = userService.createUser(user);

        // Create role-specific profile
        Long profileId = null;
        String profileName = null;
        boolean isProfileVerified = false;

        switch (request.getRole()) {
            case BUSINESS_OWNER -> {
                if (request.hasBusinessInfo()) {
                    BusinessProfile businessProfile = createBusinessProfile(savedUser, request);
                    profileId = businessProfile.getId();
                    profileName = businessProfile.getBusinessName();
                    isProfileVerified = businessProfile.isVerified();
                }
            }
            case SELLER -> {
                if (request.hasSellerInfo()) {
                    SellerProfile sellerProfile = createSellerProfile(savedUser, request);
                    profileId = sellerProfile.getId();
                    profileName = sellerProfile.getShopName();
                    isProfileVerified = sellerProfile.isVerified();
                }
            }
        }

        // Generate tokens
        Map<String, Object> extraClaims = createTokenClaims(savedUser, profileId);
        String accessToken = jwtUtil.generateToken(extraClaims, savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .refreshExpiresIn(jwtUtil.getRefreshExpirationTime())
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(savedUser.getId())
                        .firstName(savedUser.getFirstName())
                        .lastName(savedUser.getLastName())
                        .email(savedUser.getEmail())
                        .phoneNumber(savedUser.getPhoneNumber())
                        .role(savedUser.getRole())
                        .isEmailVerified(savedUser.isEmailVerified())
                        .isAccountLocked(savedUser.isAccountLocked())
                        .lastLogin(savedUser.getLastLogin())
                        .createdAt(savedUser.getCreatedAt())
                        .profileId(profileId)
                        .profileName(profileName)
                        .isProfileVerified(isProfileVerified)
                        .build())
                .build();
    }

    /**
     * Authenticate user login
     */
    public AuthenticationResponse login(LoginRequest request) {
        log.info("User login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = (User) userDetails;

            // Reset failed login attempts on successful login
            userService.resetFailedLoginAttempts(user.getEmail());

            // Update last login
            userService.updateLastLogin(user.getId());

            // Get profile information
            ProfileInfo profileInfo = getProfileInfo(user);

            // Generate tokens
            Map<String, Object> extraClaims = createTokenClaims(user, profileInfo.profileId());
            String accessToken = jwtUtil.generateToken(extraClaims, user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .refreshExpiresIn(jwtUtil.getRefreshExpirationTime())
                    .user(AuthenticationResponse.UserInfo.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .role(user.getRole())
                            .isEmailVerified(user.isEmailVerified())
                            .isAccountLocked(user.isAccountLocked())
                            .lastLogin(user.getLastLogin())
                            .createdAt(user.getCreatedAt())
                            .profileId(profileInfo.profileId())
                            .profileName(profileInfo.profileName())
                            .isProfileVerified(profileInfo.isVerified())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            userService.incrementFailedLoginAttempts(request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refresh access token
     */
    public AuthenticationResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userEmail = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(userEmail);
        User user = (User) userDetails;

        // Get profile information
        ProfileInfo profileInfo = getProfileInfo(user);

        // Generate new access token
        Map<String, Object> extraClaims = createTokenClaims(user, profileInfo.profileId());
        String newAccessToken = jwtUtil.generateToken(extraClaims, user);

        log.debug("Access token refreshed for user: {}", userEmail);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .refreshExpiresIn(jwtUtil.getRefreshExpirationTime())
                .user(AuthenticationResponse.UserInfo.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .isEmailVerified(user.isEmailVerified())
                        .isAccountLocked(user.isAccountLocked())
                        .lastLogin(user.getLastLogin())
                        .createdAt(user.getCreatedAt())
                        .profileId(profileInfo.profileId())
                        .profileName(profileInfo.profileName())
                        .isProfileVerified(profileInfo.isVerified())
                        .build())
                .build();
    }

    /**
     * Validate registration request
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.isPasswordMatching()) {
            throw new RuntimeException("Password and confirm password do not match");
        }

        if (userService.emailExists(request.getEmail())) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        // Validate role-specific requirements
        if (request.getRole() == UserRole.BUSINESS_OWNER && !request.hasBusinessInfo()) {
            throw new RuntimeException("Business information is required for business owner registration");
        }

        if (request.getRole() == UserRole.SELLER && !request.hasSellerInfo()) {
            throw new RuntimeException("Shop information is required for seller registration");
        }
    }

    /**
     * Create business profile
     */
    private BusinessProfile createBusinessProfile(User user, RegisterRequest request) {
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.setUser(user);
        businessProfile.setBusinessName(request.getBusinessName());
        businessProfile.setBusinessDescription(request.getBusinessDescription());
        businessProfile.setBusinessEmail(request.getBusinessEmail());
        businessProfile.setBusinessPhone(request.getBusinessPhone());
        businessProfile.setBusinessRegistrationNumber(request.getBusinessRegistrationNumber());
        businessProfile.setTaxId(request.getTaxId());
        businessProfile.setBusinessAddress(request.getBusinessAddress());
        businessProfile.setBusinessCity(request.getBusinessCity());
        businessProfile.setBusinessState(request.getBusinessState());
        businessProfile.setBusinessPostalCode(request.getBusinessPostalCode());
        businessProfile.setBusinessCountry(request.getBusinessCountry());
        businessProfile.setWebsiteUrl(request.getWebsiteUrl());

        return businessProfileRepository.save(businessProfile);
    }

    /**
     * Create seller profile
     */
    private SellerProfile createSellerProfile(User user, RegisterRequest request) {
        SellerProfile sellerProfile = new SellerProfile();
        sellerProfile.setUser(user);
        sellerProfile.setShopName(request.getShopName());
        sellerProfile.setShopDescription(request.getShopDescription());
        sellerProfile.setContactEmail(request.getContactEmail());
        sellerProfile.setContactPhone(request.getContactPhone());
        sellerProfile.setTaxId(request.getTaxId());
        sellerProfile.setAddress(request.getAddress());
        sellerProfile.setCity(request.getCity());
        sellerProfile.setState(request.getState());
        sellerProfile.setPostalCode(request.getPostalCode());
        sellerProfile.setCountry(request.getCountry());
        sellerProfile.setBankAccountInfo(request.getBankAccountInfo());

        return sellerProfileRepository.save(sellerProfile);
    }

    /**
     * Get profile information for a user
     */
    private ProfileInfo getProfileInfo(User user) {
        return switch (user.getRole()) {
            case BUSINESS_OWNER -> businessProfileRepository.findByUser(user)
                    .map(bp -> new ProfileInfo(bp.getId(), bp.getBusinessName(), bp.isVerified()))
                    .orElse(new ProfileInfo(null, null, false));
            case SELLER -> sellerProfileRepository.findByUser(user)
                    .map(sp -> new ProfileInfo(sp.getId(), sp.getShopName(), sp.isVerified()))
                    .orElse(new ProfileInfo(null, null, false));
            default -> new ProfileInfo(null, null, false);
        };
    }

    /**
     * Create JWT token claims
     */
    private Map<String, Object> createTokenClaims(User user, Long profileId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("emailVerified", user.isEmailVerified());
        if (profileId != null) {
            claims.put("profileId", profileId);
        }
        return claims;
    }

    /**
     * Profile information record
     */
    private record ProfileInfo(Long profileId, String profileName, boolean isVerified) {
    }
}