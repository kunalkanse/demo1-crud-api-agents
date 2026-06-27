package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.IProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController (API Layer).
 * Tests HTTP endpoints with MockMvc and security context.
 */
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    public void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Bluetooth Headphones");
        product.setDescription("Over-ear noise-cancelling headphones");
        product.setPrice(new BigDecimal("89.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(150);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productDTO = new ProductDTO();
        productDTO.setName("Wireless Bluetooth Headphones");
        productDTO.setDescription("Over-ear noise-cancelling headphones");
        productDTO.setPrice(new BigDecimal("89.99"));
        productDTO.setCategory("Electronics");
        productDTO.setStockQuantity(150);
    }

    // ==================== GET by ID Tests ====================

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testGetProductById_Positive_ProductRetrievedSuccessfully() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Wireless Bluetooth Headphones")))
                .andExpect(jsonPath("$.price", is(89.99)));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testGetProductById_Negative_ProductNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException("Product with id 999 not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testGetProductById_InvalidInput_InvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetProductById_NoAuth_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET All Products Tests ====================

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testGetAllProducts_Positive_AllProductsRetrieved() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Wireless Bluetooth Headphones")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testGetAllProducts_NoAuth_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CREATE Tests ====================

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testCreateProduct_Positive_ProductCreatedSuccessfully() throws Exception {
        // Arrange
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(product);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Wireless Bluetooth Headphones")))
                .andExpect(jsonPath("$.price", is(89.99)));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testCreateProduct_InvalidInput_MissingRequiredFields() throws Exception {
        // Arrange
        ProductDTO invalidDTO = new ProductDTO();
        invalidDTO.setName(""); // Empty name

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testCreateProduct_InvalidInput_NegativePrice() throws Exception {
        // Arrange
        productDTO.setPrice(new BigDecimal("-10.00"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testCreateProduct_InvalidInput_NegativeStockQuantity() throws Exception {
        // Arrange
        productDTO.setStockQuantity(-5);

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testCreateProduct_NoManager_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateProduct_NoAuth_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== UPDATE Tests ====================

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testUpdateProduct_Positive_ProductUpdatedSuccessfully() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("149.99"));
        updatedProduct.setCategory("Electronics");
        updatedProduct.setStockQuantity(200);

        when(productService.updateProduct(anyLong(), any(ProductDTO.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.price", is(149.99)));

        verify(productService, times(1)).updateProduct(anyLong(), any(ProductDTO.class));
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testUpdateProduct_Negative_ProductNotFound() throws Exception {
        // Arrange
        when(productService.updateProduct(999L, productDTO))
                .thenThrow(new ProductNotFoundException("Product with id 999 not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testUpdateProduct_InvalidInput_InvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/products/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testUpdateProduct_NoManager_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE Tests ====================

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testDeleteProduct_Positive_ProductDeletedSuccessfully() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testDeleteProduct_Negative_ProductNotFound() throws Exception {
        // Arrange
        doThrow(new ProductNotFoundException("Product with id 999 not found"))
                .when(productService).deleteProduct(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_MANAGER")
    public void testDeleteProduct_InvalidInput_InvalidId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PRODUCT_VIEWER")
    public void testDeleteProduct_NoManager_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
