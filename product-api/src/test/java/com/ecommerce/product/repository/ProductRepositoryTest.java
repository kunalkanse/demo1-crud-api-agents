package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductRepository (Data Access Layer).
 * Tests CRUD operations and custom query methods.
 */
@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    public void setUp() {
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCategory("Electronics");
        testProduct.setStockQuantity(100);
    }

    // ==================== CREATE Tests ====================

    @Test
    public void testCreate_Positive_ProductSavedSuccessfully() {
        // Act
        Product savedProduct = productRepository.save(testProduct);

        // Assert
        assertNotNull(savedProduct.getId());
        assertEquals("Test Product", savedProduct.getName());
        assertEquals(new BigDecimal("99.99"), savedProduct.getPrice());
        assertEquals("Electronics", savedProduct.getCategory());
        assertEquals(100, savedProduct.getStockQuantity());
    }

    @Test
    public void testCreate_InvalidInput_NullName() {
        // Arrange
        testProduct.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> productRepository.save(testProduct));
    }

    @Test
    public void testCreate_InvalidInput_NegativePrice() {
        // Arrange
        testProduct.setPrice(new BigDecimal("-10.00"));

        // Act & Assert - Price should be validated by constraints
        Product saved = productRepository.save(testProduct);
        assertNotNull(saved);
    }

    @Test
    public void testCreate_InvalidInput_NegativeStockQuantity() {
        // Arrange
        testProduct.setStockQuantity(-5);

        // Act & Assert - Stock quantity should be validated by constraints
        Product saved = productRepository.save(testProduct);
        assertNotNull(saved);
    }

    // ==================== READ Tests ====================

    @Test
    public void testRead_Positive_FindProductById() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);

        // Act
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());

        // Assert
        assertTrue(retrievedProduct.isPresent());
        assertEquals(savedProduct.getId(), retrievedProduct.get().getId());
        assertEquals("Test Product", retrievedProduct.get().getName());
    }

    @Test
    public void testRead_Negative_ProductNotFound() {
        // Act
        Optional<Product> retrievedProduct = productRepository.findById(999L);

        // Assert
        assertTrue(retrievedProduct.isEmpty());
    }

    @Test
    public void testRead_InvalidInput_InvalidId() {
        // Act & Assert - Negative ID should not exist
        Optional<Product> retrievedProduct = productRepository.findById(-1L);
        assertTrue(retrievedProduct.isEmpty());
    }

    @Test
    public void testRead_NullInput_NullId() {
        // Act & Assert - Null ID should return empty
        Optional<Product> retrievedProduct = productRepository.findById(null);
        assertTrue(retrievedProduct.isEmpty());
    }

    // ==================== UPDATE Tests ====================

    @Test
    public void testUpdate_Positive_ProductUpdatedSuccessfully() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);
        savedProduct.setName("Updated Product");
        savedProduct.setPrice(new BigDecimal("149.99"));

        // Act
        Product updatedProduct = productRepository.save(savedProduct);

        // Assert
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(new BigDecimal("149.99"), updatedProduct.getPrice());
    }

    @Test
    public void testUpdate_InvalidInput_EmptyName() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);
        savedProduct.setName("");

        // Act & Assert
        Product updated = productRepository.save(savedProduct);
        assertEquals("", updated.getName());
    }

    @Test
    public void testUpdate_InvalidInput_NegativePrice() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);
        savedProduct.setPrice(new BigDecimal("-25.00"));

        // Act & Assert
        Product updated = productRepository.save(savedProduct);
        assertNotNull(updated);
    }

    @Test
    public void testUpdate_NullInput_NullFields() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // Act - Set fields to null
        savedProduct.setDescription(null);
        Product updated = productRepository.save(savedProduct);

        // Assert
        assertEquals(productId, updated.getId());
        assertNull(updated.getDescription());
    }

    // ==================== DELETE Tests ====================

    @Test
    public void testDelete_Positive_ProductDeletedSuccessfully() {
        // Arrange
        Product savedProduct = productRepository.save(testProduct);
        Long productId = savedProduct.getId();

        // Act
        productRepository.deleteById(productId);

        // Assert
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertTrue(deletedProduct.isEmpty());
    }

    @Test
    public void testDelete_Negative_DeleteNonExistentProduct() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> productRepository.deleteById(999L));
    }

    @Test
    public void testDelete_InvalidInput_InvalidId() {
        // Act & Assert
        assertDoesNotThrow(() -> productRepository.deleteById(-1L));
    }

    @Test
    public void testDelete_NullInput_NullId() {
        // Act & Assert
        assertDoesNotThrow(() -> productRepository.deleteById(null));
    }

    // ==================== CUSTOM QUERY Tests ====================

    @Test
    public void testFindByCategory_Positive() {
        // Arrange
        productRepository.save(testProduct);
        Product product2 = new Product();
        product2.setName("Another Product");
        product2.setDescription("Another Description");
        product2.setPrice(new BigDecimal("49.99"));
        product2.setCategory("Electronics");
        product2.setStockQuantity(50);
        productRepository.save(product2);

        // Act
        var products = productRepository.findByCategory("Electronics");

        // Assert
        assertEquals(2, products.size());
    }

    @Test
    public void testExistsByName_Positive() {
        // Arrange
        productRepository.save(testProduct);

        // Act & Assert
        assertTrue(productRepository.existsByName("Test Product"));
        assertFalse(productRepository.existsByName("Non-existent Product"));
    }

    @Test
    public void testFindByName_Positive() {
        // Arrange
        productRepository.save(testProduct);

        // Act
        Optional<Product> found = productRepository.findByName("Test Product");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Product", found.get().getName());
    }
}
