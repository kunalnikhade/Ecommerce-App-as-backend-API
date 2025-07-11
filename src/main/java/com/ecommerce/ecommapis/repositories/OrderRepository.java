package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>
{
    @Query("SELECT o FROM OrderEntity o WHERE o.user.id = :userId")
    List<OrderEntity> findAllOrdersByUserId(@Param("userId") UUID userId);
}
