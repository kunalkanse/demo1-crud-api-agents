package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product business logic.
 * Defines all product-related operations.
 */
public interface IProductService {

    /**
     * Create a new product.
     *
     * @param productDTO the product data transfer object
     * @return the created product with generated ID
     */
    Product createProduct(ProductDTO productDTO);

    /**
     * Retrieve a product by ID.
     *
     * @param id the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> getProductById(Long id);

    /**
     * Retrieve all products.
     *
     * @return list of all products
     */
    List<Product> getAllProducts();

    /**
     * Update an existing product.
     *
     * @param id the product ID
     * @param productDTO the updated product data
     * @return the updated product
     */
    Product updateProduct(Long id, ProductDTO productDTO);

    /**
     * Delete a product by ID.
     *
     * @param id the product ID
     */
    void deleteProduct(Long id);

    /**
     * Check if a product exists by ID.
     *
     * @param id the product ID
     * @return true if product exists, false otherwise
     */
    boolean productExists(Long id);
}
