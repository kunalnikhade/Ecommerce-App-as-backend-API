package com.ecommerce.ecommapis.dto.order;

import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDto
{
    private UUID orderId;

    @NotNull(message = "Order date must not be null")
    private Date orderDate;

    @NotNull(message = "Order status must not be null")
    private OrderStatusEnums orderStatus;

    @NotNull(message = "User ID must not be null")
    private UUID userId;

    private double deliveryCharge;

    @NotNull(message = "Order items must not be null")
    private List<OrderItemDto> orderItems;

    @NotNull(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotNull(message = "Amount is required")
    private int amount;
}
