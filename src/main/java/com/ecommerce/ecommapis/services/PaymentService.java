package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.PaymentDto;
import com.ecommerce.ecommapis.enumerations.OrderStatusEnums;
import com.ecommerce.ecommapis.enumerations.PaymentStatus;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.model.OrderEntity;
import com.ecommerce.ecommapis.model.PaymentEntity;
import com.ecommerce.ecommapis.repositories.OrderRepository;
import com.ecommerce.ecommapis.repositories.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService
{
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(final PaymentRepository paymentRepository, final OrderRepository orderRepository)
    {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentDto initiatePayment(final PaymentDto paymentDto, final UUID userId, final UUID orderId)
    {
        final OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId))
        {
            throw new ResourceNotFoundException("User not found");
        }

        final boolean paymentExists = paymentRepository.existsByOrder(order);

        // Prevent duplicate payments
        if (paymentExists)
        {
            throw new ResourceNotFoundException("Payment has already been processed for this order");
        }

        // Calculate total amount from order
        double totalAmount = order.getOrderTotal();

        // Add delivery charge if needed
        double deliveryCharge = totalAmount < 700 ? 40.0 : 0.0;

        totalAmount += deliveryCharge;

        final PaymentEntity payment = convertToEntity(paymentDto);

        payment.setOrder(order);
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(paymentDto.getPaymentStatus());

        final PaymentEntity savedPayment = paymentRepository.save(payment);

        // If payment was successful, update the order status
        if (savedPayment.getPaymentStatus() == PaymentStatus.SUCCESS)
        {
            order.setOrderStatus(OrderStatusEnums.CONFIRMED);

            orderRepository.save(order);
        }

        return convertToDto(savedPayment);
    }

    @Transactional
    public PaymentDto getStatusByPaymentId(final UUID paymentId)
    {
        final PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        payment.setId(payment.getId());

        return convertToDto(payment);
    }

    private PaymentDto convertToDto(final PaymentEntity paymentEntity)
    {
        final PaymentDto paymentDto = new PaymentDto();

        paymentDto.setAmount(paymentEntity.getAmount());
        paymentDto.setPaymentStatus(paymentEntity.getPaymentStatus());
        paymentDto.setPaymentMethod(paymentEntity.getPaymentMethod());
        paymentDto.setPaymentDateTime(paymentEntity.getPaymentDateTime());

        return paymentDto;
    }

    private PaymentEntity convertToEntity(final PaymentDto paymentDto)
    {
        final PaymentEntity paymentEntity = new PaymentEntity();

        // paymentEntity.setAmount(paymentDto.getAmount()); (removed cause it sets in initiatePayment method)
        paymentEntity.setPaymentStatus(paymentDto.getPaymentStatus());
        paymentEntity.setPaymentMethod(paymentDto.getPaymentMethod());
        paymentEntity.setPaymentDateTime(LocalDateTime.now());

        return paymentEntity;
    }
}
