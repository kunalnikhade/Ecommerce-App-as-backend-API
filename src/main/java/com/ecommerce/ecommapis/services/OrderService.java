package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.auth.MailBody;
import com.ecommerce.ecommapis.dto.order.*;
import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import com.ecommerce.ecommapis.exception.*;
import com.ecommerce.ecommapis.model.*;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import com.ecommerce.ecommapis.model.cart.*;
import com.ecommerce.ecommapis.model.order.*;
import com.ecommerce.ecommapis.repositories.*;
import com.ecommerce.ecommapis.services.auth.EmailService;
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
    private final EmailService emailService;

    public OrderService(final OrderRepository orderRepository,
                        final CartRepository cartRepository,
                        final ProductRepository productRepository,
                        final CartService cartService,
                        final RazorpayClient razorpayClient,
                        final EmailService emailService)
    {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.razorpayClient = razorpayClient;
        this.emailService = emailService;
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

    public void updateOrderStatus(final UUID orderId, final OrderStatusEnums status)
    {
        final OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setOrderStatus(status);

        orderRepository.save(order);

        // send status updated email to user
        final MailBody mailBody = MailBody.builder()
                .to(order.getUser().getEmail())
                .subject("Update on Your Order #" + order.getOrderId())
                .body(
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<style>" +
                                "  body { font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 0; margin: 0; }" +
                                "  .container { background-color: #fff; padding: 20px; border-radius: 10px; max-width: 600px; margin: 30px auto; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }" +
                                "  .header { font-size: 22px; font-weight: bold; color: #4CAF50; margin-bottom: 15px; }" +
                                "  .content { font-size: 16px; line-height: 1.6; }" +
                                "  .status-box { background-color: #e8f4ff; padding: 12px 18px; border-radius: 5px; margin: 20px 0; display: inline-block; font-weight: bold; color: #0d47a1; border-left: 6px solid #2196F3; }" +
                                "  .footer { margin-top: 30px; font-size: 14px; color: #888; text-align: center; border-top: 1px solid #eee; padding-top: 15px; }" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "  <div class='container'>" +
                                "    <div class='header'>Order Update Notification</div>" +
                                "    <div class='content'>" +
                                "      <p>Hi <strong>" + order.getUser().getName() + "</strong>,</p>" +
                                "      <p>" +
                                (status.name().equals("DELIVERED")
                                        ? "We're happy to let you know that your order <strong>#" + order.getOrderId() + "</strong> has been <strong>successfully delivered</strong>. We hope you're enjoying your purchase!"
                                        : status.name().equals("OUT_FOR_DELIVERY")
                                        ? "Your order <strong>#" + order.getOrderId() + "</strong> is currently <strong>out for delivery</strong>. Please be available to receive it."
                                        : "Your order <strong>#" + order.getOrderId() + "</strong> status has been updated.") +
                                "      </p>" +
                                "      <div class='status-box'>Current Status: " + status.name() + "</div>" +
                                (status.name().equals("OUT_FOR_DELIVERY")
                                        ? "<p><strong>Delivery Agent Contact:</strong> " + "9370218623" + "</p>"
                                        : "") +
                                "      <p>" +
                                (status.name().equals("DELIVERED")
                                        ? "If you have any feedback or need help with returns, feel free to reach out."
                                        : "We'll continue to keep you updated as your order progresses.") +
                                "      </p>" +
                                "      <p>Thank you for choosing us! 🛍️</p>" +
                                "    </div>" +
                                "    <div class='footer'>— The E-Commerce Team</div>" +
                                "  </div>" +
                                "</body>" +
                                "</html>"
                )
                .build();

        emailService.sendSimpleMessage(mailBody);
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
