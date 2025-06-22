package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.Cart;
import com.dnofulla.marketcove.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Get cart with cart items for user
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.item WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}