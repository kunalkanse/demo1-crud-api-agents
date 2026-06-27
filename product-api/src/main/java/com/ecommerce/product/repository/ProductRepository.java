package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository interface for Product entity.
 * Provides CRUD operations and query methods.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by category name.
     *
     * @param category the product category
     * @return list of products in the category
     */
    List<Product> findByCategory(String category);

    /**
     * Check if a product exists by name.
     *
     * @param name the product name
     * @return true if product exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Find product by name.
     *
     * @param name the product name
     * @return Optional containing the product if found
     */
    Optional<Product> findByName(String name);
}
