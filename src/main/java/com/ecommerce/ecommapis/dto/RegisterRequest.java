package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

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
