package com.ecommerce.ecommapis.services;

import com.ecommerce.ecommapis.exception.UserNameNotFoundException;
import com.ecommerce.ecommapis.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService
{
    private final UserRepository userRepository;

    @Autowired
    public MyUserDetailsService(final UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException
    {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNameNotFoundException("User not found with username" + username));
    }
}
