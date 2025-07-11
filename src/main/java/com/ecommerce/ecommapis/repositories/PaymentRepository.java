package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.OrderEntity;
import com.ecommerce.ecommapis.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID>
{

    boolean existsByOrder(final OrderEntity order);
}
