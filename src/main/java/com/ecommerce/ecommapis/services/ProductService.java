package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.ProductDto;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.model.ProductEntity;
import com.ecommerce.ecommapis.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ProductService
{
    private final static Logger log = Logger.getLogger(ProductService.class.getName());
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(final ProductRepository productRepository)
    {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductDto addProducts(final ProductDto productDto)
    {
        ProductEntity productEntity = convertToEntity(productDto);
        productEntity = productRepository.save(productEntity);

        return convertToDto(productEntity);
    }

    @Transactional
    public ProductDto updateProductById(final UUID id, final ProductDto productDto)
    {
        final ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id : " + id));

        productEntity.setPName(productDto.getName());
        productEntity.setPDescription(productDto.getDescription());
        productEntity.setPImageUrl(productDto.getImageURL());
        productEntity.setPPrice(productDto.getPrice());
        productEntity.setCategory(productDto.getCategory());
        productEntity.setQuantity(productDto.getQuantity());

        return convertToDto(productRepository.save(productEntity));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> allProducts()
    {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> productsByName(final String name)
    {
        final List<ProductEntity> productEntities = productRepository.findByPName(name);

        if (productEntities.isEmpty())
        {
            throw new ResourceNotFoundException("Product not found with name " + name);
        }

        return productEntities.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> ProductsByCategory(final String category)
    {
        final List<ProductEntity> productEntities = productRepository.findByCategory(category);

        if (productEntities.isEmpty())
        {
            throw new ResourceNotFoundException("No products found with category " + category);
        }

        return productEntities.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchByPriceRange(final Double minPrice, final Double maxPrice)
    {
        final List<ProductEntity> productEntities = productRepository.findByPPriceBetween(minPrice, maxPrice);

        if (productEntities.isEmpty())
        {
            throw new ResourceNotFoundException("No products found with price range" + minPrice + " and " + maxPrice);
        }

        return productEntities.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> productsBySearchText(final String searchText)
    {
        final List<ProductEntity> productEntities = productRepository.findProductBySearchTextAndPriceRange(searchText);

        if (productEntities.isEmpty())
        {
            throw new ResourceNotFoundException("No products found with search text " + searchText);
        }

        return productEntities.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public void deleteAllProducts()
    {
        productRepository.deleteAll();
    }

    @Transactional
    public void deleteProductById(final UUID id)
    {
        final ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id " + id));

        productRepository.delete(productEntity);
    }

    private ProductDto convertToDto(final ProductEntity productEntity)
    {
        final ProductDto productDto = new ProductDto();

        productDto.setName(productEntity.getPName());
        productDto.setDescription(productEntity.getPDescription());
        productDto.setImageURL(productEntity.getPImageUrl());
        productDto.setPrice(productEntity.getPPrice());
        productDto.setCategory(productEntity.getCategory());
        productDto.setQuantity(productEntity.getQuantity());

        return productDto;
    }

    private ProductEntity convertToEntity(final ProductDto productDto)
    {
        final ProductEntity productEntity = new ProductEntity();

        productEntity.setPName(productDto.getName());
        productEntity.setPDescription(productDto.getDescription());
        productEntity.setPImageUrl(productDto.getImageURL());
        productEntity.setPPrice(productDto.getPrice());
        productEntity.setCategory(productDto.getCategory());
        productEntity.setQuantity(productDto.getQuantity());

        return productEntity;
    }
}
