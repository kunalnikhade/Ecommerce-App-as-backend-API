package com.ecommerce.ecommapis.controllers;

import com.ecommerce.ecommapis.dto.LoginRequest;
import com.ecommerce.ecommapis.dto.RegisterRequest;
import com.ecommerce.ecommapis.model.UserEntity;
import com.ecommerce.ecommapis.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController
{
    private final AuthService authService;

    @Autowired
    public AuthController(final AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserEntity> register(@RequestBody final RegisterRequest registerRequest)
    {
        return new ResponseEntity<>(authService.getRegister(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody final LoginRequest loginRequest)
    {
        try
        {
            return new ResponseEntity<>(authService.verify(loginRequest), HttpStatus.OK);
        }
        catch (final Exception e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
