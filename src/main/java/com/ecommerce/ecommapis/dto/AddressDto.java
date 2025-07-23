package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddressDto
{
    @NotNull(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Street is required")
    private String street;

    @NotNull(message = "City is required")
    private String city;

    @NotNull(message = "State is required")
    private String state;

    @NotNull(message = "Country is required")
    private String country;

    @NotNull(message = "Postal code is required")
    private String postalCode;

    @NotNull(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "User ID is required")
    private UUID userId;
}
