package com.ecommerce.ecommapis.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDto
{
    @NotNull(message = "Product ID cannot be null")
    private UUID productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(
            value = 1,
            message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "ID cannot be null")
    private UUID userId;

    @NotNull(message = "Cart items cannot be null")
    private List<CartItemDto> cartItems;
}
