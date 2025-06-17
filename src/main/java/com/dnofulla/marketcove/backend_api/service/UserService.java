package com.dnofulla.marketcove.backend_api.service;

import com.dnofulla.marketcove.backend_api.entity.User;
import com.dnofulla.marketcove.backend_api.enums.UserRole;
import com.dnofulla.marketcove.backend_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for User entity operations and Spring Security integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Load user by username (email) for Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Create a new user
     */
    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new RuntimeException("User already exists with email: " + user.getEmail());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate email verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());

        // Set default values
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find users by role
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Update user's last login time
     */
    public void updateLastLogin(Long userId) {
        userRepository.updateLastLogin(userId, LocalDateTime.now());
        log.debug("Updated last login for user ID: {}", userId);
    }

    /**
     * Increment failed login attempts and lock account if necessary
     */
    public void incrementFailedLoginAttempts(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            log.warn("Incremented failed login attempts for user: {}. Total attempts: {}",
                    email, user.getFailedLoginAttempts());
        }
    }

    /**
     * Reset failed login attempts
     */
    public void resetFailedLoginAttempts(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.resetFailedLoginAttempts();
            userRepository.save(user);
            log.info("Reset failed login attempts for user: {}", email);
        }
    }

    /**
     * Verify user's email
     */
    public boolean verifyEmail(String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            userRepository.save(user);
            log.info("Email verified for user: {}", user.getEmail());
            return true;
        }
        return false;
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24)); // 24 hours expiry
            userRepository.save(user);
            log.info("Password reset token generated for user: {}", email);
            return token;
        }
        throw new RuntimeException("User not found with email: " + email);
    }

    /**
     * Reset password using token
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByPasswordResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiry(null);
                user.resetFailedLoginAttempts(); // Reset failed attempts on successful password reset
                userRepository.save(user);
                log.info("Password reset successfully for user: {}", user.getEmail());
                return true;
            } else {
                log.warn("Password reset token expired for user: {}", user.getEmail());
            }
        }
        return false;
    }

    /**
     * Update user information
     */
    public User updateUser(User user) {
        log.info("Updating user with ID: {}", user.getId());
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("User deleted with ID: {}", id);
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Get user statistics
     */
    public UserStats getUserStats() {
        long totalUsers = userRepository.count();
        long verifiedUsers = userRepository.countByIsEmailVerifiedTrue();
        long customers = userRepository.countByRole(UserRole.CUSTOMER);
        long sellers = userRepository.countByRole(UserRole.SELLER);
        long businessOwners = userRepository.countByRole(UserRole.BUSINESS_OWNER);
        long admins = userRepository.countByRole(UserRole.ADMIN);

        return new UserStats(totalUsers, verifiedUsers, customers, sellers, businessOwners, admins);
    }

    /**
     * Clean up expired password reset tokens
     */
    @Transactional
    public void cleanupExpiredTokens() {
        List<User> usersWithExpiredTokens = userRepository.findUsersWithExpiredPasswordResetTokens(LocalDateTime.now());
        for (User user : usersWithExpiredTokens) {
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
        }
        log.info("Cleaned up {} expired password reset tokens", usersWithExpiredTokens.size());
    }

    /**
     * User statistics record
     */
    public record UserStats(
            long totalUsers,
            long verifiedUsers,
            long customers,
            long sellers,
            long businessOwners,
            long admins) {
    }
}