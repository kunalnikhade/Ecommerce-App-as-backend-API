package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.ProductDto;
import com.ecommerce.ecommapis.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/api/products")
public class ProductController
{
    private final ProductService productService;

    @Autowired
    public ProductController(final ProductService productService)
    {
        this.productService = productService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/admin/add",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<ProductDto> addProducts(@RequestBody final ProductDto productDto)
    {
        return new ResponseEntity<>(productService.addProducts(productDto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            value = "/admin/update/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<ProductDto> updateProductById(@PathVariable final UUID id, @RequestBody final ProductDto productDto)
    {
        return new ResponseEntity<>(productService.updateProductById(id, productDto), HttpStatus.OK);
    }

    @GetMapping(
            value = "/all",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ProductDto>> getAllProducts()
    {
        return new ResponseEntity<>(productService.allProducts(), HttpStatus.OK);
    }

    @GetMapping(
            value = "/product/{name}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ProductDto>> getProductsByName(@PathVariable final String name)
    {
        return new ResponseEntity<>(productService.productsByName(name), HttpStatus.OK);
    }

    @GetMapping(
            value = "/category/{category}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable final String category)
    {
        return new ResponseEntity<>(productService.ProductsByCategory(category), HttpStatus.OK);
    }

    @GetMapping(
            value = "/price-range",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ProductDto>> getProductsByPriceRange(@RequestParam final Double minPrice, @RequestParam final Double maxPrice)
    {
        return new ResponseEntity<>(productService.searchByPriceRange(minPrice, maxPrice), HttpStatus.OK);
    }

    @GetMapping(
            value = "/searchText",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<ProductDto>> getProductsBySearchText(@RequestParam final String searchText)
    {
        return new ResponseEntity<>(productService.productsBySearchText(searchText), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(
            value = "/admin/deleteAll",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> deleteAll()
    {
        productService.deleteAllProducts();

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(
            value = "/admin/delete/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> deleteProductById(@PathVariable final UUID id)
    {
        productService.deleteProductById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
