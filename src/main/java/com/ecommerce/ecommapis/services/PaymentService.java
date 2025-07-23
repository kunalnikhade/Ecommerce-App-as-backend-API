package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.PaymentDto;
import com.ecommerce.ecommapis.enumerations.*;
import com.ecommerce.ecommapis.exception.*;
import com.ecommerce.ecommapis.model.order.OrderEntity;
import com.ecommerce.ecommapis.model.PaymentEntity;
import com.ecommerce.ecommapis.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService
{
    @Value("${razorpay.secret}")
    private String razorpaySecret;

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
            throw new PaymentException("Payment has already been processed for this order");
        }

        // Verify Razorpay Signature
        if (!isValidSignature(
                paymentDto.getRazorpayOrderId(),
                paymentDto.getRazorpayPaymentId(),
                paymentDto.getRazorpaySignature()))
        {
            throw new PaymentException("Invalid Razorpay signature");
        }

        // Calculate total amount from order
        double totalAmount = order.getOrderTotal();

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

    // Razorpay Signature Validator
    private boolean isValidSignature(final String razorpayOrderId,
                                     final String razorpayPaymentId,
                                     final String razorpaySignature)
    {
        try
        {
            final String payload = razorpayOrderId + "|" + razorpayPaymentId;
            final Mac mac = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secretKey = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] digest = mac.doFinal(payload.getBytes());

            // Convert byte[] to HEX string
            final StringBuilder hexString = new StringBuilder();
            for (byte b : digest)
            {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            final String calculatedSignature = hexString.toString();

            return calculatedSignature.equals(razorpaySignature);
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    private PaymentDto convertToDto(final PaymentEntity paymentEntity)
    {
        final PaymentDto paymentDto = new PaymentDto();

        paymentDto.setAmount(paymentEntity.getAmount());
        paymentDto.setPaymentStatus(paymentEntity.getPaymentStatus());
        paymentDto.setPaymentMethod(paymentEntity.getPaymentMethod());
        paymentDto.setPaymentDateTime(paymentEntity.getPaymentDateTime());
        paymentDto.setRazorpayPaymentId(paymentEntity.getRazorpayPaymentId());
        paymentDto.setRazorpayOrderId(paymentEntity.getRazorpayOrderId());
        paymentDto.setRazorpaySignature(paymentEntity.getRazorpaySignature());

        return paymentDto;
    }

    private PaymentEntity convertToEntity(final PaymentDto paymentDto)
    {
        final PaymentEntity paymentEntity = new PaymentEntity();

        // paymentEntity.setAmount(paymentDto.getAmount()); (removed cause it sets in initiatePayment method)
        paymentEntity.setPaymentStatus(paymentDto.getPaymentStatus());
        paymentEntity.setPaymentMethod(paymentDto.getPaymentMethod());
        paymentEntity.setPaymentDateTime(LocalDateTime.now());
        paymentEntity.setRazorpayPaymentId(paymentDto.getRazorpayPaymentId());
        paymentEntity.setRazorpayOrderId(paymentDto.getRazorpayOrderId());
        paymentEntity.setRazorpaySignature(paymentDto.getRazorpaySignature());

        return paymentEntity;
    }
}
