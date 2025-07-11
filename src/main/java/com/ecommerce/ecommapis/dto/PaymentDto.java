package com.ecommerce.ecommapis.dto;

import com.ecommerce.ecommapis.enumerations.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentDto
{
    private Double amount;

    @NotNull(message = "Method is required")
    private String paymentMethod;

    @NotNull(message = "Status is required")
    private PaymentStatus paymentStatus;

    @NotNull(message = "PaymentDateTime is required")
    private LocalDateTime paymentDateTime;
}