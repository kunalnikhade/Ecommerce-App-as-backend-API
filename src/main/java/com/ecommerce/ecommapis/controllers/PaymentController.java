package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.PaymentDto;
import com.ecommerce.ecommapis.services.PaymentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/payment")
public class PaymentController
{
    private final PaymentService paymentService;

    public PaymentController(final PaymentService paymentService)
    {
        this.paymentService = paymentService;
    }

    @CrossOrigin(origins = "http://localhost:63342")
    @PostMapping(value = "/order/{orderId}")
    public ResponseEntity<PaymentDto> makePayment(@RequestBody final PaymentDto paymentDto,
                                                  @RequestParam final UUID userId,
                                                  @PathVariable final UUID orderId)
    {
        return new ResponseEntity<>(paymentService.initiatePayment(paymentDto, userId, orderId), HttpStatus.OK);
    }

    @GetMapping(value = "/status/{paymentId}")
    public ResponseEntity<PaymentDto> getStatus(@PathVariable final UUID paymentId)
    {
        return new ResponseEntity<>(paymentService.getStatusByPaymentId(paymentId), HttpStatus.OK);
    }
}
