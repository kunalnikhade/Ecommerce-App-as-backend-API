package com.ecommerce.ecommapis.services.auth;

import com.ecommerce.ecommapis.dto.auth.ResetPasswordDto;
import com.ecommerce.ecommapis.exception.ResourceNotFoundException;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import com.ecommerce.ecommapis.repositories.auth.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordService(final UserRepository userRepository, final PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void resetPassword(final String email, final ResetPasswordDto resetPasswordDto)
    {
        final UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(resetPasswordDto.getOldPassword(), user.getPassword()))
        {
            throw new ResourceNotFoundException("Old password does not match");
        }

        if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getRepeatPassword()))
        {
            throw new ResourceNotFoundException("New password does not match");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));

        userRepository.save(user);
    }
}
