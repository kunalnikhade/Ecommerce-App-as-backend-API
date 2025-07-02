package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.CartEntity;
import com.ecommerce.ecommapis.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, UUID>
{
    Optional<CartEntity> findByUser(final UserEntity user);
}
