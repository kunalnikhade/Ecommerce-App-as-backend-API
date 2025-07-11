package com.ecommerce.ecommapis.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest
{
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 6,
            message = "Password should be strong")
    private String password;
}
