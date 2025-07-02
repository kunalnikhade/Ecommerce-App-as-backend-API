package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.dto.LoginRequest;
import com.ecommerce.ecommapis.dto.RegisterRequest;
import com.ecommerce.ecommapis.enumerations.UserRole;
import com.ecommerce.ecommapis.model.UserEntity;
import com.ecommerce.ecommapis.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public AuthService(final UserRepository userRepository, final PasswordEncoder passwordEncoder, final AuthenticationManager authenticationManager, final JwtService jwtService)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserEntity getRegister(final RegisterRequest registerRequest)
    {
        final UserEntity user = new UserEntity();

        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setMobileNumber(registerRequest.getMobileNumber());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(UserRole.USER);

        return userRepository.save(user);
    }

    public String verify(final LoginRequest loginRequest)
    {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        if (authentication.isAuthenticated())
        {
            // The principal contains the authenticated UserDetails
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // It's safer and cleaner to rely on the already loaded user from authentication
            final Optional<UserEntity> optionalUser = userRepository.findByEmail(userDetails.getUsername());

            if (optionalUser.isPresent())
            {
                final UserEntity user = optionalUser.get();

                return jwtService.generateToken(userDetails.getUsername(), user.getRole().name());
            }
        }

        return "failed to authenticate";
    }
}
