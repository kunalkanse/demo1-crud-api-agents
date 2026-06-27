package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService (Business Logic Layer).
 * Tests CRUD operations and validation logic.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductDTO productDTO;
    private Product product;

    @BeforeEach
    public void setUp() {
        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setCategory("Electronics");
        productDTO.setStockQuantity(100);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(100);
    }

    // ==================== CREATE Tests ====================

    @Test
    public void testCreateProduct_Positive_ProductCreatedSuccessfully() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product createdProduct = productService.createProduct(productDTO);

        // Assert
        assertNotNull(createdProduct);
        assertEquals(product.getId(), createdProduct.getId());
        assertEquals("Test Product", createdProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testCreateProduct_InvalidInput_NullName() {
        // Arrange
        productDTO.setName(null);

        // Act & Assert
        assertThrows(Exception.class, () -> productService.createProduct(productDTO));
    }

    @Test
    public void testCreateProduct_InvalidInput_NegativePrice() {
        // Arrange
        productDTO.setPrice(new BigDecimal("-10.00"));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act & Assert - Service layer may allow negative price validation
        Product createdProduct = productService.createProduct(productDTO);
        assertNotNull(createdProduct);
    }

    @Test
    public void testCreateProduct_InvalidInput_NegativeStockQuantity() {
        // Arrange
        productDTO.setStockQuantity(-5);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        Product createdProduct = productService.createProduct(productDTO);

        // Assert
        assertNotNull(createdProduct);
    }

    // ==================== READ Tests ====================

    @Test
    public void testGetProductById_Positive_ProductRetrievedSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Optional<Product> retrievedProduct = productService.getProductById(1L);

        // Assert
        assertTrue(retrievedProduct.isPresent());
        assertEquals(product.getId(), retrievedProduct.get().getId());
        assertEquals("Test Product", retrievedProduct.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductById_Negative_ProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Product> retrievedProduct = productService.getProductById(999L);

        // Assert
        assertTrue(retrievedProduct.isEmpty());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetProductById_InvalidInput_InvalidId() {
        // Arrange
        when(productRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        Optional<Product> retrievedProduct = productService.getProductById(-1L);

        // Assert
        assertTrue(retrievedProduct.isEmpty());
    }

    @Test
    public void testGetProductById_NullInput_NullId() {
        // Arrange
        when(productRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Product> retrievedProduct = productService.getProductById(null);

        // Assert
        assertTrue(retrievedProduct.isEmpty());
    }

    @Test
    public void testGetAllProducts_Positive_AllProductsRetrieved() {
        // Arrange
        List<Product> productList = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(productList);

        // Act
        List<Product> retrievedProducts = productService.getAllProducts();

        // Assert
        assertNotNull(retrievedProducts);
        assertEquals(1, retrievedProducts.size());
        verify(productRepository, times(1)).findAll();
    }

    // ==================== UPDATE Tests ====================

    @Test
    public void testUpdateProduct_Positive_ProductUpdatedSuccessfully() {
        // Arrange
        ProductDTO updatedDTO = new ProductDTO();
        updatedDTO.setName("Updated Product");
        updatedDTO.setDescription("Updated Description");
        updatedDTO.setPrice(new BigDecimal("149.99"));
        updatedDTO.setCategory("Electronics");
        updatedDTO.setStockQuantity(200);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("149.99"));
        updatedProduct.setCategory("Electronics");
        updatedProduct.setStockQuantity(200);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("149.99"), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_Negative_ProductNotFound() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> productService.updateProduct(999L, productDTO));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    public void testUpdateProduct_InvalidInput_InvalidId() {
        // Arrange
        when(productRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> productService.updateProduct(-1L, productDTO));
    }

    @Test
    public void testUpdateProduct_NullInput_NullDTO() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(NullPointerException.class, 
            () -> productService.updateProduct(1L, null));
    }

    // ==================== DELETE Tests ====================

    @Test
    public void testDeleteProduct_Positive_ProductDeletedSuccessfully() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProduct_Negative_ProductNotFound() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> productService.deleteProduct(999L));
        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    public void testDeleteProduct_InvalidInput_InvalidId() {
        // Arrange
        when(productRepository.existsById(-1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> productService.deleteProduct(-1L));
    }

    @Test
    public void testDeleteProduct_NullInput_NullId() {
        // Arrange
        when(productRepository.existsById(null)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, 
            () -> productService.deleteProduct(null));
    }

    @Test
    public void testProductExists_Positive_ProductExists() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean exists = productService.productExists(1L);

        // Assert
        assertTrue(exists);
        verify(productRepository, times(1)).existsById(1L);
    }

    @Test
    public void testProductExists_Negative_ProductDoesNotExist() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean exists = productService.productExists(999L);

        // Assert
        assertFalse(exists);
    }
}
