package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDto
{
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String description;

    @NotBlank(message = "Product image is required")
    private String imageURL;

    @NotNull(message = "Product price is required")
    private Double price;

    @NotNull(message = "Product category is required")
    private String category;

    @NotNull(message = "Product quantity is required")
    private Integer quantity;
}
