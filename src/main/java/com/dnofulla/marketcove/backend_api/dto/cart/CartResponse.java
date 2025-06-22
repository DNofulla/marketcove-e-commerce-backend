package com.dnofulla.marketcove.backend_api.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> cartItems;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private boolean empty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}