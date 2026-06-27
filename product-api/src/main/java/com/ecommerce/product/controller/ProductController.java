package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for Product CRUD operations.
 * All endpoints require Bearer JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product CRUD API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final IProductService productService;

    /**
     * Retrieve a product by its ID.
     * Requires authentication but any authenticated user can access.
     *
     * @param id the product ID
     * @return the product if found, 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCT_VIEWER', 'PRODUCT_MANAGER')")
    @Operation(summary = "Get product by ID", description = "Retrieve detailed information about a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    /**
     * Retrieve all products.
     * Requires authentication but any authenticated user can access.
     *
     * @return list of all products
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PRODUCT_VIEWER', 'PRODUCT_MANAGER')")
    @Operation(summary = "Get all products", description = "Retrieve a list of all products in the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Create a new product.
     * Requires PRODUCT_MANAGER role.
     *
     * @param productDTO the product data
     * @return the created product with HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('PRODUCT_MANAGER')")
    @Operation(summary = "Create a new product", description = "Add a new product to the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        Product createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Update an existing product.
     * Requires PRODUCT_MANAGER role.
     *
     * @param id the product ID to update
     * @param productDTO the updated product data
     * @return the updated product with HTTP 200
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCT_MANAGER')")
    @Operation(summary = "Update a product", description = "Update details of an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Product updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product.
     * Requires PRODUCT_MANAGER role.
     *
     * @param id the product ID to delete
     * @return HTTP 204 No Content if successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCT_MANAGER')")
    @Operation(summary = "Delete a product", description = "Remove a product from the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
