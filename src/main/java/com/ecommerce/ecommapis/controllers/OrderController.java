package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.OrderDto;
import com.ecommerce.ecommapis.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/order")
public class OrderController
{
    private final OrderService orderService;

    public OrderController(final OrderService orderService)
    {
        this.orderService = orderService;
    }

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

    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<OrderDto> getAllOrdersByUserId(@PathVariable final UUID userId)
    {
        return new ResponseEntity<>(orderService.allOrdersByUserId(userId), HttpStatus.OK);
    }
}
