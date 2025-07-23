package com.ecommerce.ecommapis.repositories.auth;

import com.ecommerce.ecommapis.model.auth.ForgotPasswordEntity;
import com.ecommerce.ecommapis.model.auth.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPasswordEntity, UUID>
{
    Optional<ForgotPasswordEntity> findByUser(final UserEntity user);

    @Query("SELECT forgotPassword " +
            "FROM ForgotPasswordEntity forgotPassword " +
            "WHERE forgotPassword.otp = ?1 AND forgotPassword.user = ?2 AND forgotPassword.expirationTime >= CURRENT_TIMESTAMP")
    Optional<ForgotPasswordEntity> findByOtpAndUser(final Integer otp, final UserEntity user);
}
