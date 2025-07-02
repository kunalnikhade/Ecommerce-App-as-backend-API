package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>
{
    Optional<UserEntity> findByEmail(final String username);
}
