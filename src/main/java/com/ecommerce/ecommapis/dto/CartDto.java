package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

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
