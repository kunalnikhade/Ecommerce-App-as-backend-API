package com.ecommerce.ecommapis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "orderItems")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderItemEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderItemId", unique = true, nullable = false)
    private UUID orderItemId;

    @Column(name = "orderQuantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "orderItemPrice")
    private Double orderItemPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;
}
