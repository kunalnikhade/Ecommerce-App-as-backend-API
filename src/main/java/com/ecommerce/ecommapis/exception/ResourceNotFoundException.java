package com.ecommerce.ecommapis.exception;

public class ResourceNotFoundException extends RuntimeException
{
    public ResourceNotFoundException(final String message)
    {
        super(message);
    }
}
