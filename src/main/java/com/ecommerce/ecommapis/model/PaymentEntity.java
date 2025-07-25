package com.ecommerce.ecommapis.model;

import com.ecommerce.ecommapis.enumerations.PaymentStatus;
import com.ecommerce.ecommapis.model.order.OrderEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            unique = true,
            nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(name = "amount")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentStatus")
    private PaymentStatus paymentStatus;

    @Column(name = "paymentMethod")
    private String paymentMethod;

    @Column(name = "paymentDateTime")
    private LocalDateTime paymentDateTime;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;
}
