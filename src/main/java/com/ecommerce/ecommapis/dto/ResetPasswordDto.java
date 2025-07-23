package com.ecommerce.ecommapis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDto
{
    @NotNull(message = "Old password is required")
    private String oldPassword;

    @NotNull(message = "New password is required")
    private String newPassword;

    @NotNull(message = "Repeat password is required")
    private String repeatPassword;
}
