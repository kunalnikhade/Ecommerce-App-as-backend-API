package com.ecommerce.ecommapis.dto;

import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDto
{
    @NotNull(message = "Order date must not be null")
    private Date orderDate;

    @NotNull(message = "Order status must not be null")
    private OrderStatusEnums orderStatus;

    @NotNull(message = "User ID must not be null")
    private UUID userId;

    @NotNull(message = "Order items must not be null")
    private List<OrderItemDto> orderItems;
}
