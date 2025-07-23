package com.ecommerce.ecommapis.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequest
{
    @NotBlank(message = "Users name is required")
    private String name;

    @NotBlank(message = "Users email is required")
    @Email
    private String email;

    @NotBlank(message = "Users username is required")
    private String username;

    @NotNull
    @Size(
            min = 10,
            max = 10,
            message = "Mobile number is required")
    private Long mobileNumber;

    @NotBlank(message = "Users password is required")
    @Size(
            min = 6,
            message = "Password should be strong")
    private String password;
}
