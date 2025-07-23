package com.ecommerce.ecommapis.controllers.auth;

import com.ecommerce.ecommapis.dto.auth.ResetPasswordDto;
import com.ecommerce.ecommapis.services.auth.ResetPasswordService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reset-password")
public class ResetPasswordController
{
    private final ResetPasswordService resetPasswordService;

    public ResetPasswordController(ResetPasswordService resetPasswordService)
    {
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping(value = "/reset/{email}",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> resetPassword(@PathVariable final String email,
                                                @RequestBody final ResetPasswordDto resetPasswordDto)
    {
        resetPasswordService.resetPassword(email, resetPasswordDto);

        return new ResponseEntity<>("Reset password successfully", HttpStatus.OK);
    }
}
