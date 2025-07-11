package com.ecommerce.ecommapis.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderItemDto
{
    @NotNull(message = "Order quantity must not be null")
    private Integer orderQuantity;

    @NotNull(message = "Order price must not be null")
    private Double orderPrice;

    @NotNull(message = "Product ID must not be null")
    private UUID productId;
}
