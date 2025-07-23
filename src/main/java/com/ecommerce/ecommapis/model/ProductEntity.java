package com.ecommerce.ecommapis.model;

import com.ecommerce.ecommapis.model.cart.CartItemEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "products")
public class ProductEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pId")
    private UUID id;

    @Column(name = "productName")
    private String pName;

    @Column(name = "productDescription")
    private String pDescription;

    @Column(name = "imageURL")
    private String pImageUrl;

    @Column(name = "productPrice")
    private Double pPrice;

    @Column(name = "categories")
    private String category;

    @Column(name = "quantity")
    private Integer quantity;

    @OneToMany(mappedBy = "product")
    private List<CartItemEntity> cartItems;
}
