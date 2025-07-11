package com.ecommerce.ecommapis.model.order;

import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import com.ecommerce.ecommapis.model.UserEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderId")
    private UUID orderId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "orderDate")
    private Date orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "orderStatus")
    private OrderStatusEnums orderStatus;

    @Column(name = "orderTotal")
    private Double orderTotal;

    @Column(name = "delivery_charge")
    private double deliveryCharge;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;
}

