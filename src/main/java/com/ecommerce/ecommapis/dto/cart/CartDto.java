package com.ecommerce.ecommapis.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDto
{
    @NotNull(message = "ID cannot be null")
    private UUID userId;

    @NotNull(message = "Cart items cannot be null")
    private List<CartItemDto> cartItems;
}
