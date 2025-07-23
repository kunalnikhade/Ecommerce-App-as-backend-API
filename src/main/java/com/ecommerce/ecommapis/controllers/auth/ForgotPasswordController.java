package com.ecommerce.ecommapis.controllers.auth;

import com.ecommerce.ecommapis.dto.auth.ChangePasswordDto;
import com.ecommerce.ecommapis.services.auth.ForgotPasswordService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/forgot-password")
public class ForgotPasswordController
{
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(final ForgotPasswordService forgotPasswordService)
    {
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping(value = "/verify-email/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable final String email)
    {
        forgotPasswordService.sendVerificationOtp(email);

        return new ResponseEntity<>("Verification email sent successfully", HttpStatus.OK);
    }

    @PostMapping(value = "/verify-otp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable final Integer otp, @PathVariable final String email)
    {
        forgotPasswordService.verifyOtp(otp, email);

        return new ResponseEntity<>("OTP verified", HttpStatus.OK);
    }

    @PostMapping(value = "/change-password/{email}",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                 produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> changePassword(@RequestBody final ChangePasswordDto changePasswordDto,
                                                 @PathVariable final String email)
    {
        forgotPasswordService.changePassword(email, changePasswordDto);

        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }
}
