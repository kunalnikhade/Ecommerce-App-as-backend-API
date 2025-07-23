
package com.ecommerce.ecommapis.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto
{
    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Repeat password is required")
    private String repeatPassword;
}
