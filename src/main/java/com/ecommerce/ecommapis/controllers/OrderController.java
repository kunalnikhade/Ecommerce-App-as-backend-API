package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.order.OrderDto;
import com.ecommerce.ecommapis.services.OrderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/api/order")
public class OrderController
{
    private final OrderService orderService;

    public OrderController(final OrderService orderService)
    {
        this.orderService = orderService;
    }

    @CrossOrigin(origins = "http://localhost:63342")
    @PostMapping(value = "/placeOrder/{cartId}")
    public ResponseEntity<OrderDto> getPlaceAnOrder(@PathVariable final UUID cartId)
    {
        return new ResponseEntity<>(orderService.placeOrder(cartId), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable final UUID orderId)
    {
        return new ResponseEntity<>(orderService.orderById(orderId), HttpStatus.OK);
    }

    @GetMapping(value = "/allOrders/user/{userId}")
    public ResponseEntity<List<OrderDto>> getAllOrdersByUserId(@PathVariable final UUID userId)
    {
        return new ResponseEntity<>(orderService.allOrdersByUserId(userId), HttpStatus.OK);
    }

    @PatchMapping(value = "/cancelOrder/{userId}/{orderId}")
    public ResponseEntity<String> cancelOrderByUserId(final @PathVariable UUID userId, final @PathVariable UUID orderId)
    {
        orderService.cancelOrderByUserId(userId, orderId);

        return new ResponseEntity<>("Order is cancelled",HttpStatus.OK);
    }
}
