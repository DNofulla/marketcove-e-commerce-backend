package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.BusinessProfile;
import com.dnofulla.marketcove.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BusinessProfile entity operations
 */
@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    /**
     * Find business profile by user
     */
    Optional<BusinessProfile> findByUser(User user);

    /**
     * Find business profile by user ID
     */
    Optional<BusinessProfile> findByUserId(Long userId);

    /**
     * Find business profile by business name (case-insensitive)
     */
    Optional<BusinessProfile> findByBusinessNameIgnoreCase(String businessName);

    /**
     * Check if business name exists
     */
    boolean existsByBusinessNameIgnoreCase(String businessName);

    /**
     * Find verified business profiles
     */
    List<BusinessProfile> findByIsVerifiedTrue();

    /**
     * Find unverified business profiles
     */
    List<BusinessProfile> findByIsVerifiedFalse();

    /**
     * Find business profiles by city
     */
    List<BusinessProfile> findByBusinessCityIgnoreCase(String city);

    /**
     * Find business profiles by state
     */
    List<BusinessProfile> findByBusinessStateIgnoreCase(String state);

    /**
     * Find business profiles by country
     */
    List<BusinessProfile> findByBusinessCountryIgnoreCase(String country);

    /**
     * Find business profiles by registration number
     */
    Optional<BusinessProfile> findByBusinessRegistrationNumber(String registrationNumber);

    /**
     * Check if registration number exists
     */
    boolean existsByBusinessRegistrationNumber(String registrationNumber);

    /**
     * Count verified businesses
     */
    long countByIsVerifiedTrue();

    /**
     * Search business profiles by name or description
     */
    @Query("SELECT bp FROM BusinessProfile bp WHERE " +
            "LOWER(bp.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(bp.businessDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BusinessProfile> searchByKeyword(@Param("keyword") String keyword);
}