package com.ecommerce.ecommapis.exception;

public class InsufficientQuantityException extends RuntimeException
{
    public InsufficientQuantityException(String message)
    {
        super(message);
    }
}
