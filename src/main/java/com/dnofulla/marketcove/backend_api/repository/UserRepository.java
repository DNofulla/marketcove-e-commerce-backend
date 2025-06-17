package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (case-insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if user exists by email
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find verified users by role
     */
    List<User> findByRoleAndIsEmailVerifiedTrue(UserRole role);

    /**
     * Find user by email verification token
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find users by enabled status
     */
    List<User> findByIsEnabled(boolean isEnabled);

    /**
     * Find locked accounts
     */
    List<User> findByIsAccountLockedTrue();

    /**
     * Find users created between dates
     */
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find users by role and enabled status
     */
    List<User> findByRoleAndIsEnabledTrue(UserRole role);

    /**
     * Update last login time
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Update failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.isAccountLocked = :isLocked WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") Long userId, @Param("attempts") int attempts,
            @Param("isLocked") boolean isLocked);

    /**
     * Reset failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.isAccountLocked = false WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * Update email verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true, u.emailVerificationToken = null WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") Long userId);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Count verified users
     */
    long countByIsEmailVerifiedTrue();

    /**
     * Find users with expired password reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken IS NOT NULL AND u.passwordResetTokenExpiry < :now")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);
}