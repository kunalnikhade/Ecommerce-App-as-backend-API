package com.ecommerce.ecommapis.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "forgotPassword")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ForgotPasswordEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "forgotPasswordId")
    private UUID forgotPasswordId;

    @Column(name = "otp")
    private Integer otp;

    @Column(name = "expirationTime")
    private Date expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
