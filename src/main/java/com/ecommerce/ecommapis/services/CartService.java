package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.CartDto;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.exception.UserNameNotFoundException;
import com.ecommerce.ecommapis.model.CartEntity;
import com.ecommerce.ecommapis.model.CartItemEntity;
import com.ecommerce.ecommapis.model.ProductEntity;
import com.ecommerce.ecommapis.model.UserEntity;
import com.ecommerce.ecommapis.repositories.CartRepository;
import com.ecommerce.ecommapis.repositories.ProductRepository;
import com.ecommerce.ecommapis.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService
{
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(final CartRepository cartRepository,
                       final ProductRepository productRepository,
                       final UserRepository userRepository)
    {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CartDto addProductToCart(final CartDto cartDto)
    {
        final UserEntity user = userRepository.findById(cartDto.getUserId())
                .orElseThrow(() -> new UserNameNotFoundException("User Not Found"));

        final ProductEntity product = productRepository.findById(cartDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantity() == null || product.getQuantity() < cartDto.getQuantity())
        {
            throw new ResourceNotFoundException("Insufficient product quantity available");
        }

        // Find or create cart
        CartEntity cartEntity = cartRepository.findByUser(user).orElseGet(() ->
        {
            final CartEntity newCart = new CartEntity();

            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());

            return newCart;
        });

        // Check if product already exists in cart
        Optional<CartItemEntity> existingItemOpt = cartEntity.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartDto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent())
        {
            final CartItemEntity existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartDto.getQuantity());
        }
        else
        {
            final CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cartEntity)
                    .product(product)
                    .quantity(cartDto.getQuantity())
                    .price(product.getPPrice())
                    .build();

            cartEntity.getCartItems().add(newItem);
        }

        return convertToDto(cartRepository.save(cartEntity));
    }

    @Transactional
    public CartDto updateCartItemById(final UUID cartItemId, final UUID cartId, final CartDto cartDto)
    {
        final CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        final CartItemEntity cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItem.setQuantity(cartItem.getQuantity() + cartDto.getQuantity());

        return convertToDto(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public List<CartDto> getCartItems()
    {
        return cartRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public void removeProductFromCart(final UUID cartId, final UUID cartItemId)
    {
        final CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        final CartItemEntity removeCartItem = cart.getCartItems().stream()
                        .filter(item -> item.getId().equals(cartItemId))
                        .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getCartItems().remove(removeCartItem);

        cartRepository.save(cart);
    }

    private CartDto convertToDto(final CartEntity cartEntity)
    {
        final CartDto cartDto = new CartDto();

        cartDto.setUserId(cartEntity.getUser().getId());

        if (!cartEntity.getCartItems().isEmpty())
        {
            CartItemEntity item = cartEntity.getCartItems().get(0);
            cartDto.setProductId(item.getProduct().getId());
            cartDto.setQuantity(item.getQuantity());
        }

        return cartDto;
    }

    private CartEntity convertToEntity(final CartDto cartDto)
    {
        // Fetch user and product entities using IDs from DTO
        UserEntity user = userRepository.findById(cartDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductEntity product = productRepository.findById(cartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Create CartEntity
        CartEntity cartEntity = new CartEntity();

        cartEntity.setCartId(UUID.randomUUID());
        cartEntity.setUser(user);

        // Create CartItemEntity
        CartItemEntity item = CartItemEntity.builder()
                .cart(cartEntity)
                .product(product)
                .quantity(cartDto.getQuantity())
                .price(product.getPPrice())
                .build();

        cartEntity.setCartItems(List.of(item));

        return cartEntity;
    }
}
