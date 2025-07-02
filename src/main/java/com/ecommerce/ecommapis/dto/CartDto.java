package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
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
}
