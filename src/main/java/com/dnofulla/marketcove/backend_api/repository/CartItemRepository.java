package com.dnofulla.marketcove.backend_api.repository;

import com.dnofulla.marketcove.backend_api.entity.Cart;
import com.dnofulla.marketcove.backend_api.entity.CartItem;
import com.dnofulla.marketcove.backend_api.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart and item
     */
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    /**
     * Find all cart items by cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find cart items by cart ID
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find cart item by cart ID and item ID
     */
    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    /**
     * Delete all cart items by cart
     */
    void deleteByCart(Cart cart);

    /**
     * Count cart items by cart
     */
    long countByCart(Cart cart);

    /**
     * Find cart items by user ID
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);

    /**
     * Delete cart item by cart and item
     */
    void deleteByCartAndItem(Cart cart, Item item);
}