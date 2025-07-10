package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.OrderDto;
import com.ecommerce.ecommapis.dto.OrderItemDto;
import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.model.*;
import com.ecommerce.ecommapis.repositories.CartRepository;
import com.ecommerce.ecommapis.repositories.OrderRepository;
import com.ecommerce.ecommapis.repositories.ProductRepository;
import com.ecommerce.ecommapis.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService
{
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderService(final OrderRepository orderRepository,
                        final CartRepository cartRepository,
                        final ProductRepository productRepository,
                        final UserRepository userRepository,
                        final CartService cartService)
    {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderDto placeOrder(final UUID cartId)
    {
        final CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        final List<CartItemEntity> cartItems = cart.getCartItems();

        if (cartItems == null || cartItems.isEmpty())
        {
            throw new ResourceNotFoundException("Cart is empty for ID: " + cartId);
        }

        final UserEntity user = cart.getUser();

        double totalAmount = 0.0;

        final List<OrderItemEntity> orderItems = new ArrayList<>();

        for (CartItemEntity cartItem : cartItems)
        {
            final ProductEntity product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity())
            {
                throw new ResourceNotFoundException("Insufficient stock for product: " + product.getId());
            }

            // Subtract stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Calculate total
            double itemTotal = product.getPPrice() * cartItem.getQuantity();
            totalAmount += itemTotal;

            // Create order item
            final OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setProduct(product);
            orderItem.setOrderQuantity(cartItem.getQuantity());
            orderItem.setOrderItemPrice(product.getPPrice());
            orderItems.add(orderItem);
        }

        // Create and save order
        final OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatusEnums.PENDING);
        order.setOrderTotal(totalAmount);

        for (OrderItemEntity item : orderItems)
        {
            item.setOrder(order); // bi-directional mapping
        }

        order.setOrderItems(orderItems);

        // Save order
        final OrderEntity savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCartByCartId(cartId);

        return convertToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto orderById(final UUID orderId)
    {
        final OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return convertToDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto allOrdersByUserId(UUID userId)
    {
        final UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return null;
    }

    private OrderDto convertToDto(final OrderEntity orderEntity)
    {
        final OrderDto orderDto = new OrderDto();

        orderDto.setOrderDate(orderEntity.getOrderDate());
        orderDto.setOrderStatus(orderEntity.getOrderStatus());
        orderDto.setUserId(orderEntity.getUser().getId());

        // Convert OrderItemEntity list to OrderItemDto list
        if (orderEntity.getOrderItems() != null)
        {
            List<OrderItemDto> itemDtos = orderEntity.getOrderItems().stream().map(item ->
            {
                final OrderItemDto dto = new OrderItemDto();

                dto.setOrderQuantity(item.getOrderQuantity());
                dto.setOrderPrice(item.getOrderItemPrice());
                dto.setProductId(item.getProduct().getId());

                return dto;

            }).toList();

            orderDto.setOrderItems(itemDtos);
        }

        return orderDto;
    }

    private OrderEntity convertToEntity(final OrderDto orderDto)
    {
        final OrderEntity orderEntity = new OrderEntity();

        orderEntity.setOrderDate(orderDto.getOrderDate());
        orderEntity.setOrderStatus(
                orderDto.getOrderStatus() != null ? orderDto.getOrderStatus() : OrderStatusEnums.PENDING
        );

        final double total = orderDto.getOrderItems().stream()
                .mapToDouble(item -> item.getOrderQuantity() * item.getOrderPrice())
                .sum();

        orderEntity.setOrderTotal(total);

        // map items
        final List<OrderItemEntity> itemEntities = orderDto.getOrderItems().stream().map(dto ->
        {
            final OrderItemEntity item = new OrderItemEntity();

            item.setOrderQuantity(dto.getOrderQuantity());
            item.setOrderItemPrice(dto.getOrderPrice());

            final ProductEntity product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            item.setProduct(product);
            item.setOrder(orderEntity);

            return item;

        }).toList();

        orderEntity.setOrderItems(itemEntities);

        return orderEntity;
    }
}
