package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID>
{
}
