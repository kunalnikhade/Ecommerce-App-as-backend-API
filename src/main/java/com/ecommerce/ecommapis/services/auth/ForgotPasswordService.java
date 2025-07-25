package com.ecommerce.ecommapis.services.auth;

import com.ecommerce.ecommapis.dto.auth.ChangePasswordDto;
import com.ecommerce.ecommapis.dto.auth.MailBody;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.model.auth.ForgotPasswordEntity;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import com.ecommerce.ecommapis.repositories.auth.ForgotPasswordRepository;
import com.ecommerce.ecommapis.repositories.auth.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ForgotPasswordService
{
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordService(final ForgotPasswordRepository forgotPasswordRepository,
                                 final UserRepository userRepository,
                                 final EmailService emailService,
                                 final PasswordEncoder passwordEncoder)
    {
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendVerificationOtp(final String email)
    {
        final UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final Integer generatedOtp = generateOtp();

        // send forgot password email to user
        final MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP for Password Forgot")
                .body(
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<style>" +
                                "  body { font-family: Arial, sans-serif; background-color: #f7f7f7; color: #333; padding: 0; margin: 0; }" +
                                "  .container { background-color: #fff; padding: 20px; border-radius: 8px; max-width: 500px; margin: 30px auto; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); }" +
                                "  .header { font-size: 20px; font-weight: bold; color: #4CAF50; margin-bottom: 15px; }" +
                                "  .otp { font-size: 24px; font-weight: bold; color: #000; background-color: #f1f1f1; padding: 10px 20px; border-radius: 5px; display: inline-block; margin: 15px 0; }" +
                                "  .warning { color: #d32f2f; font-weight: bold; }" +
                                "  .footer { font-size: 13px; color: #777; margin-top: 25px; }" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "  <div class='container'>" +
                                "    <div class='header'>🔐 Password Forgot Request</div>" +
                                "    <p>We received a request to forgot your password. Use the OTP below to proceed:</p>" +
                                "    <div class='otp'>" + generatedOtp + "</div>" +
                                "    <p class='warning'>⚠️ Do not share this OTP with anyone. We never ask for your password or OTP.</p>" +
                                "    <p>If you did not request this, please ignore this email.</p>" +
                                "    <div class='footer'>Thank you, <br>The E-Commerce Team</div>" +
                                "  </div>" +
                                "</body>" +
                                "</html>"
                )
                .build();

        // Check if forgot password already exists for the user
        final ForgotPasswordEntity forgotPassword = forgotPasswordRepository.findByUser(user)
                .orElse(ForgotPasswordEntity.builder().user(user).build());

        // update or create
        forgotPassword.setOtp(generatedOtp);
        forgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + 70 * 1000));

        emailService.sendSimpleMessage(mailBody);

        forgotPasswordRepository.save(forgotPassword);
    }

    public void verifyOtp(Integer otp, String email)
    {
        final UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final ForgotPasswordEntity forgotPassword = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new ResourceNotFoundException("Forgot Password not found"));

        // Validate expiration time
        if (forgotPassword.getExpirationTime().before(new Date()))
        {
            throw new ResourceNotFoundException("OTP has expired");
        }

        // Invalidate OTP after use
        forgotPasswordRepository.delete(forgotPassword);
    }

    public void changePassword(final String email, final ChangePasswordDto changePasswordDto)
    {
        if (!Objects.equals(changePasswordDto.getPassword(), changePasswordDto.getRepeatPassword()))
        {
            throw new ResourceNotFoundException("Passwords do not match!");
        }

        // Check if user exists first
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        final String encryptedPassword = passwordEncoder.encode(changePasswordDto.getPassword());

        userRepository.updatePassword(email, encryptedPassword);
    }

    private Integer generateOtp()
    {
        final Random random = new Random();

        return random.nextInt(100_000, 999_999);
    }
}
