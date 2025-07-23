package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.order.*;
import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import com.ecommerce.ecommapis.exception.*;
import com.ecommerce.ecommapis.model.*;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import com.ecommerce.ecommapis.model.cart.*;
import com.ecommerce.ecommapis.model.order.*;
import com.ecommerce.ecommapis.repositories.*;
import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService
{
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final RazorpayClient razorpayClient;

    public OrderService(final OrderRepository orderRepository,
                        final CartRepository cartRepository,
                        final ProductRepository productRepository,
                        final CartService cartService,
                        final RazorpayClient razorpayClient)
    {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.razorpayClient = razorpayClient;
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

        for (final CartItemEntity cartItem : cartItems)
        {
            final ProductEntity product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantity() < cartItem.getQuantity())
            {
                throw new InsufficientQuantityException("Insufficient stock for product: " + product.getId());
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
        order.setOrderId(order.getOrderId());
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatusEnums.PENDING);

        // Add delivery charges
        double deliveryCharges = totalAmount < 700 ? 40.0 : 0.0;
        double finalTotalAmount = totalAmount + deliveryCharges;

        order.setOrderTotal(finalTotalAmount);
        order.setDeliveryCharge(deliveryCharges);

        for (final OrderItemEntity item : orderItems)
        {
            item.setOrder(order); // bi-directional mapping
        }

        order.setOrderItems(orderItems);

        // Create Razorpay Order
        try
        {
            int amountInPaise = (int) (finalTotalAmount * 100);

            final JSONObject options = new JSONObject();

            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 30));

            final Order razorpayOrder = razorpayClient.orders.create(options);

            order.setRazorpayOrderId(razorpayOrder.get("id"));

        }
        catch (final Exception e)
        {
            throw new FailedToCreateException("Failed to create Razorpay order" + e);
        }

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
    public List<OrderDto> allOrdersByUserId(final UUID userId)
    {
        return orderRepository.findAllOrdersByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public void cancelOrderByUserId(final UUID userId, final UUID orderId)
    {
        final Optional<OrderEntity> order = orderRepository.findById(orderId);

        if(order.isPresent())
        {
            final OrderEntity orderEntity = order.get();

            if(orderEntity.getUser().getId().equals(userId))
            {
                orderEntity.setOrderStatus(OrderStatusEnums.CANCELLED);
            }
        }
        else
        {
            throw new ResourceNotFoundException("Order not found");
        }
    }

    private OrderDto convertToDto(final OrderEntity orderEntity)
    {
        final OrderDto orderDto = new OrderDto();

        orderDto.setOrderId(orderEntity.getOrderId());
        orderDto.setOrderDate(orderEntity.getOrderDate());
        orderDto.setOrderStatus(orderEntity.getOrderStatus());
        orderDto.setUserId(orderEntity.getUser().getId());
        orderDto.setRazorpayOrderId(orderEntity.getRazorpayOrderId());
        orderDto.setAmount((int)(orderEntity.getOrderTotal() * 100));
        orderDto.setDeliveryCharge(orderEntity.getDeliveryCharge());

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

//    private OrderEntity convertToEntity(final OrderDto orderDto)
//    {
//        final OrderEntity orderEntity = new OrderEntity();
//
//        orderEntity.setOrderId(orderDto.getOrderId());
//        orderEntity.setOrderDate(orderDto.getOrderDate());
//        orderEntity.setOrderStatus(
//                orderDto.getOrderStatus() != null ? orderDto.getOrderStatus() : OrderStatusEnums.PENDING
//        );
//
//        final double total = orderDto.getOrderItems().stream()
//                .mapToDouble(item -> item.getOrderQuantity() * item.getOrderPrice())
//                .sum();
//
//        orderEntity.setOrderTotal(total);
//        orderEntity.setDeliveryCharge(orderDto.getDeliveryCharge());
//
//        // map items
//        final List<OrderItemEntity> itemEntities = orderDto.getOrderItems().stream().map(dto ->
//        {
//            final OrderItemEntity item = new OrderItemEntity();
//
//            item.setOrderQuantity(dto.getOrderQuantity());
//            item.setOrderItemPrice(dto.getOrderPrice());
//
//            final ProductEntity product = productRepository.findById(dto.getProductId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
//
//            item.setProduct(product);
//            item.setOrder(orderEntity);
//
//            return item;
//
//        }).toList();
//
//        orderEntity.setOrderItems(itemEntities);
//
//        return orderEntity;
//    }
}
