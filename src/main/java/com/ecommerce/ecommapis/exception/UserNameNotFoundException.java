package com.ecommerce.ecommapis.exception;

public class UserNameNotFoundException extends RuntimeException
{
    public UserNameNotFoundException(String message)
    {
        super(message);
    }
}
