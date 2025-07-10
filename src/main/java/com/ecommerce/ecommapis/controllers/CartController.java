package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.CartDto;
import com.ecommerce.ecommapis.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/cart")
public class CartController
{
    private final CartService cartService;

    @Autowired
    public CartController(final CartService cartService)
    {
        this.cartService = cartService;
    }

    @PostMapping(value = "/addProduct")
    public ResponseEntity<CartDto> addToCart(@RequestBody final CartDto cartDto)
    {
        return new ResponseEntity<>(cartService.addProductToCart(cartDto), HttpStatus.OK);
    }

    @PutMapping(value = "/updateCartItem/{cartId}/{cartItemId}")
    public ResponseEntity<CartDto> updateQuantityByCartItemId(@PathVariable final UUID cartItemId,
                                                              @PathVariable final UUID cartId,
                                                              @RequestBody final CartDto cartDto)
    {
        return new ResponseEntity<>(cartService.updateCartItemById(cartItemId, cartId, cartDto), HttpStatus.OK);
    }

    @GetMapping(value = "/all-CartItems")
    public ResponseEntity<List<CartDto>> getAllCarts()
    {
        return new ResponseEntity<>(cartService.getCartItems(), HttpStatus.OK);
    }

    @DeleteMapping(value = "/removeProduct/{cartId}/{cartItemId}")
    public ResponseEntity<Void> removeProductFromCart(@PathVariable final UUID cartId, @PathVariable final UUID cartItemId)
    {
        cartService.removeProductFromCart(cartId, cartItemId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/clear/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable final UUID cartId)
    {
        cartService.clearCartByCartId(cartId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
