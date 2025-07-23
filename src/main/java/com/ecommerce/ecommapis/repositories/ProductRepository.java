package com.ecommerce.ecommapis.repositories;

import com.ecommerce.ecommapis.model.ProductEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID>
{
    @Query("SELECT p FROM ProductEntity p WHERE p.pName = :name")
    List<ProductEntity> findByPName(final String name);

    List<ProductEntity> findByCategory(final String category);

    @Query("SELECT p FROM ProductEntity p WHERE p.pPrice BETWEEN :minPrice AND :maxPrice ORDER BY p.pPrice ASC")
    List<ProductEntity> findByPPriceBetween(final Double minPrice, final Double maxPrice);

    @Query(
            "SELECT p FROM ProductEntity p WHERE " +
                    "(" +
                    "LOWER(p.pName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
                    "LOWER(p.pDescription) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
                    "CAST(p.pPrice AS string) LIKE CONCAT('%', :searchText, '%') OR " +
                    "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchText, '%'))" +
                    ")")
    List<ProductEntity> findProductBySearchTextAndPriceRange(@Param("searchText") final String searchText);
}
