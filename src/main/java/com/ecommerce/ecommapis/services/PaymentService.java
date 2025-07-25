package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.PaymentDto;
import com.ecommerce.ecommapis.dto.auth.MailBody;
import com.ecommerce.ecommapis.enumerations.*;
import com.ecommerce.ecommapis.exception.*;
import com.ecommerce.ecommapis.model.order.OrderEntity;
import com.ecommerce.ecommapis.model.PaymentEntity;
import com.ecommerce.ecommapis.repositories.*;
import com.ecommerce.ecommapis.services.auth.EmailService;
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
    private final EmailService emailService;

    public PaymentService(final PaymentRepository paymentRepository,
                          final OrderRepository orderRepository,
                          final EmailService emailService)
    {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
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

            // send confirmation email to user
            final MailBody mailBody = MailBody.builder()
                    .to(order.getUser().getEmail())
                    .subject("Order Confirmed - #" + order.getOrderId())
                    .body(
                            "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "<style>" +
                                    "  body { font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; }" +
                                    "  .container { background-color: #fff; padding: 20px; border: 1px solid #eee; max-width: 600px; margin: auto; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }" +
                                    "  .header { font-size: 24px; margin-bottom: 10px; color: #0000; }" +
                                    "  .section-title { font-weight: bold; margin-top: 20px; margin-bottom: 10px; }" +
                                    "  .details { line-height: 1.6; }" +
                                    "  .footer { margin-top: 30px; font-size: 14px; color: #888; }" +
                                    "</style>" +
                                    "</head>" +
                                    "<body>" +
                                    "  <div class='container'>" +
                                    "    <div class='header'>Order Confirmed!</div>" +
                                    "    <p>Hi <strong>" + order.getUser().getName() + "</strong>,</p>" +
                                    "    <p>Thank you for your purchase! Your order has been placed successfully.</p>" +

                                    "    <div class='section-title'>🛒 Order Summary</div>" +
                                    "    <div class='details'>" +
                                    "      <p><strong>🆔 Order ID:</strong> " + order.getOrderId() + "</p>" +
                                    "      <p><strong>📅 Order Date:</strong> " + order.getOrderDate() + "</p>" +
                                    "      <p><strong>💳 Payment Method:</strong> Razorpay</p>" +
                                    "      <p><strong>💰 Subtotal:</strong> ₹" + String.format("%.2f", totalAmount) + "</p>" +
                                    "      <p><strong>🚚 Delivery Charge:</strong> ₹" + String.format("%.2f", order.getDeliveryCharge()) + "</p>" +
                                    "      <hr>" +
                                    "      <p><strong>🧾 Total Paid:</strong> ₹" + String.format("%.2f", order.getOrderTotal()) + "</p>" +
                                    "    </div>" +

                                    "    <div class='section-title'>📦 Order Status</div>" +
                                    "    <p>Your order is <strong>Confirmed</strong>. You will be notified as it moves through the stages:</p>" +
                                    "    <p>➡ Confirmed → Shipped → Out for Delivery → Delivered</p>" +

                                    "    <p>We’ll send you tracking updates as your order progresses.</p>" +

                                    "    <div class='footer'>" +
                                    "      <p>Thanks for shopping with us!<br>The E-Commerce Team 🛍️</p>" +
                                    "    </div>" +
                                    "  </div>" +
                                    "</body>" +
                                    "</html>"
                    )
                    .build();

            emailService.sendSimpleMessage(mailBody);
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
