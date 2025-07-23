package com.ecommerce.ecommapis.services.auth;

import com.ecommerce.ecommapis.dto.*;
import com.ecommerce.ecommapis.dto.auth.ChangePasswordDto;
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

        final MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP for Forgot Password Verification")
                .text("This is a email verification for forgot password is : " + generatedOtp + "\n Do not share it with Anyone !!")
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
