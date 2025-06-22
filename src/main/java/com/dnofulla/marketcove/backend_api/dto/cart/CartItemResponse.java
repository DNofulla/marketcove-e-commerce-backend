package com.dnofulla.marketcove.backend_api.dto.cart;

import com.dnofulla.marketcove.backend_api.dto.item.ItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for cart item responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id;
    private ItemResponse item;
    private Integer quantity;
    private BigDecimal priceAtTime;
    private BigDecimal subtotal;
    private boolean priceChanged;
    private BigDecimal currentItemPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}