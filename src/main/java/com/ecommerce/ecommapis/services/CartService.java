package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.CartDto;
import com.ecommerce.ecommapis.dto.CartItemDto;
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
import java.util.stream.Collectors;

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

        final CartEntity cartEntity = cartRepository.findByUser(user).orElseGet(() ->
        {
            final CartEntity newCart = new CartEntity();

            newCart.setUser(user);
            newCart.setCartItems(new ArrayList<>());

            return newCart;
        });

        for (CartItemDto itemDto : cartDto.getCartItems())
        {
            final UUID productId = itemDto.getProductId();
            final int quantity = itemDto.getQuantity();

            final ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            if (product.getQuantity() == null || product.getQuantity() < quantity)
            {
                throw new ResourceNotFoundException("Insufficient quantity for product ID: " + productId);
            }

            final Optional<CartItemEntity> existingItemOpt = cartEntity.getCartItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItemOpt.isPresent())
            {
                final CartItemEntity existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            }
            else
            {
                if (quantity <= 0)
                {
                    throw new ResourceNotFoundException("Product quantity must be greater than 0");
                }

                final CartItemEntity newItem = CartItemEntity.builder()
                        .cart(cartEntity)
                        .product(product)
                        .quantity(quantity)
                        .price(product.getPPrice())
                        .build();

                cartEntity.getCartItems().add(newItem);
            }
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

        // Find the corresponding CartItemDto from input DTO
        final CartItemDto inputItem = cartDto.getCartItems().stream()
                .filter(item -> item.getProductId().equals(cartItem.getProduct().getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No matching cart item data found in request"));

        final int requestedChange = inputItem.getQuantity();

        if (requestedChange == 0)
        {
            throw new ResourceNotFoundException("Quantity change must not be zero");
        }

        final int updatedQuantity = cartItem.getQuantity() + requestedChange;

        if (updatedQuantity < 0)
        {
            throw new ResourceNotFoundException("Resulting quantity cannot be negative");
        }

        if (updatedQuantity == 0)
        {
            cart.getCartItems().remove(cartItem); // Remove item from cart
        }
        else
        {
            cartItem.setQuantity(updatedQuantity); // Update quantity
        }

        return convertToDto(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public List<CartDto> getCartItems()
    {
        return cartRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeProductFromCart(final UUID cartId, final UUID cartItemId)
    {
        final CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        final CartItemEntity removeCartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getCartItems().remove(removeCartItem);

        cartRepository.save(cart);
    }

        @Transactional
        public void clearCartByCartId(UUID cartId)
        {
            final CartEntity cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new UserNameNotFoundException("User Not Found"));

            cart.getCartItems().clear();

            cartRepository.save(cart);
        }

    private CartDto convertToDto(final CartEntity cartEntity)
    {
        final CartDto cartDto = new CartDto();

        cartDto.setUserId(cartEntity.getUser().getId());

        final List<CartItemDto> itemDtos = cartEntity.getCartItems().stream().map(item ->
        {
            final CartItemDto itemDto = new CartItemDto();

            itemDto.setProductId(item.getProduct().getId());
            itemDto.setQuantity(item.getQuantity());

            return itemDto;

        }).collect(Collectors.toList());

        cartDto.setCartItems(itemDtos);

        return cartDto;
    }

    private CartEntity convertToEntity(final CartDto cartDto)
    {
        final UserEntity user = userRepository.findById(cartDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final CartEntity cartEntity = new CartEntity();
        cartEntity.setUser(user);

        final List<CartItemEntity> itemEntities = cartDto.getCartItems().stream().map(itemDto ->
        {
            final ProductEntity product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            return CartItemEntity.builder()
                    .cart(cartEntity)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .price(product.getPPrice())
                    .build();
        }).collect(Collectors.toList());

        cartEntity.setCartItems(itemEntities);

        return cartEntity;
    }
}
