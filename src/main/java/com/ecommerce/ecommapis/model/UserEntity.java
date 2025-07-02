package com.ecommerce.ecommapis.model;

import com.ecommerce.ecommapis.enumerations.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "users")
public class UserEntity implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            unique = true,
            nullable = false,
            name = "id")
    private UUID id;

    @Column(
            nullable = false,
            name = "name")
    private String name;

    @Column(
            nullable = false,
            name = "email")
    private String email;

    @Column(
            nullable = false,
            name = "username")
    private String username;

    @Column(
            nullable = false,
            name = "mobileNumber")
    private Long mobileNumber;

    @Column(
            nullable = false,
            name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return email;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
