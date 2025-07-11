package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.AddressDto;
import com.ecommerce.ecommapis.services.AddressService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@RestController
@RequestMapping(path = "/api/address")
public class AddressController
{
    private final AddressService addressService;

    public AddressController(final AddressService addressService)
    {
        this.addressService = addressService;
    }

    @PostMapping(value = "/add")
    public ResponseEntity<AddressDto> createAddress(@RequestBody final AddressDto address)
    {
        return new ResponseEntity<>(addressService.createNewAddress(address), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(@PathVariable final UUID addressId, @RequestBody final AddressDto addressDto)
    {
        return new ResponseEntity<>(addressService.updateAddress(addressId,addressDto), HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<AddressDto>> allAddresses()
    {
        return new ResponseEntity<>(addressService.getAllAddresses(), HttpStatus.OK);
    }

    @GetMapping(value = "/{addressId}")
    public ResponseEntity<AddressDto> addressById(@PathVariable final UUID addressId)
    {
        return new ResponseEntity<>(addressService.getAddressById(addressId), HttpStatus.OK);
    }

    @DeleteMapping(value = "/delete/{addressId}")
    public ResponseEntity<Void> deleteById(@PathVariable final UUID addressId)
    {
        addressService.getDeleteById(addressId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
