# Product CRUD API - BDD Test Cases

Feature: Product CRUD API - Retrieve Product Details by ID

  As a client application
  I want to retrieve product details using the GET product endpoint
  So that I can fetch and display accurate product information for a specific product

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"
    And the API endpoint is "/products"


# ==================== US-001: Retrieve Product Details by ID ====================

Feature: US-001 - Retrieve Product Details by ID

  As a client application
  I want to send a GET request to "/products/{id}"
  So that I can fetch and display accurate product information for a specific product

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"
    And a product with id 1 exists in the database with the following details:
      | field          | value                                                          |
      | id             | 1                                                              |
      | name           | Wireless Bluetooth Headphones                                  |
      | description    | Over-ear noise-cancelling headphones with 30-hour battery life |
      | price          | 89.99                                                          |
      | category       | Electronics                                                    |
      | stockQuantity  | 150                                                            |

  @smoke @critical
  Scenario: Successfully retrieve product details for valid product ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Content-Type" with value "application/json"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 200
    And the response header "Content-Type" is "application/json"
    And the response body contains the following fields:
      | field          | type    | value                                                          |
      | id             | integer | 1                                                              |
      | name           | string  | Wireless Bluetooth Headphones                                  |
      | description    | string  | Over-ear noise-cancelling headphones with 30-hour battery life |
      | price          | number  | 89.99                                                          |
      | category       | string  | Electronics                                                    |
      | stockQuantity  | integer | 150                                                            |

  @negative @error-handling
  Scenario: Retrieve product returns 404 Not Found for non-existent product ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    And no product exists with id 999
    When the client sends a GET request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 404
    And the response header "Content-Type" is "application/json"
    And the response body contains the following error response:
      | field   | value                          |
      | status  | 404                            |
      | error   | Not Found                      |
      | message | Product with id 999 not found  |

  @invalid-input @validation
  Scenario: Retrieve product returns 400 Bad Request for invalid (non-integer) ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/abc"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"
    And the response body contains a message about invalid ID format

  @invalid-input @validation
  Scenario: Retrieve product returns 400 Bad Request for negative ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/-1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"

  @null-input @validation
  Scenario: Retrieve product returns 400 Bad Request when ID is missing from path
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/" with missing ID parameter
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"

  @authentication @security
  Scenario: Retrieve product returns 401 Unauthorized when authentication token is missing
    When the client sends a GET request to "/products/1"
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response header "Content-Type" is "application/json"
    And the response body contains the following error response:
      | field   | value                      |
      | status  | 401                        |
      | error   | Unauthorized               |
      | message | Authentication required    |

  @authentication @security
  Scenario: Retrieve product returns 401 Unauthorized when token is invalid
    Given the client has an invalid Bearer JWT token
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <invalid_jwt_token>"
    Then the API responds with HTTP status 401
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Unauthorized"

  @authentication @security
  Scenario: Retrieve product returns 401 Unauthorized when token is expired
    Given the client has an expired Bearer JWT token
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <expired_jwt_token>"
    Then the API responds with HTTP status 401
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Unauthorized"

  @authorization @security
  Scenario: Retrieve product returns 403 Forbidden when user lacks required role
    Given the client has a valid Bearer JWT token with insufficient permissions
    And the user does NOT have "PRODUCT_VIEWER" or "PRODUCT_MANAGER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_token_no_role>"
    Then the API responds with HTTP status 403
    And the response header "Content-Type" is "application/json"
    And the response body contains the following error response:
      | field   | value              |
      | status  | 403                |
      | error   | Forbidden          |
      | message | Access Denied      |

  @authorization @security
  Scenario: Retrieve product succeeds with PRODUCT_MANAGER role (has more permissions than PRODUCT_VIEWER)
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 200
    And the response body contains the product details
    And the response body field "id" equals 1
    And the response body field "name" equals "Wireless Bluetooth Headphones"

  @content-type @response-format
  Scenario: Response Content-Type header is application/json
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 200
    And the response header "Content-Type" should be exactly "application/json"
    And the response body is valid JSON

  @response-validation @data-integrity
  Scenario: Response includes all required product fields with correct data types
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 200
    And the response body contains field "id" of type "integer"
    And the response body contains field "name" of type "string"
    And the response body contains field "description" of type "string"
    And the response body contains field "price" of type "number"
    And the response body contains field "category" of type "string"
    And the response body contains field "stockQuantity" of type "integer"
    And all response fields are non-null (except optional fields)

  @response-validation @data-integrity
  Scenario: Response field values match expected product data
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 200
    And the response body field "id" equals 1
    And the response body field "name" equals "Wireless Bluetooth Headphones"
    And the response body field "description" equals "Over-ear noise-cancelling headphones with 30-hour battery life"
    And the response body field "price" equals 89.99
    And the response body field "category" equals "Electronics"
    And the response body field "stockQuantity" equals 150


# ==================== US-002: Create a New Product ====================

Feature: US-002 - Create a New Product

  As a product manager
  I want to create a new product with all required details
  So that the product is persisted in the database and immediately available for retrieval

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"
    And the API endpoint is "/products"

  @smoke @critical
  Scenario: Successfully create product with valid details
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Content-Type" with value "application/json"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field          | value                                                          |
      | name           | Wireless Bluetooth Headphones                                  |
      | description    | Over-ear noise-cancelling headphones with 30-hour battery life |
      | price          | 89.99                                                          |
      | category       | Electronics                                                    |
      | stockQuantity  | 150                                                            |
    Then the API responds with HTTP status 201
    And the response header "Content-Type" is "application/json"
    And the response body contains generated product "id"
    And the response body contains all submitted product fields
    And the product is persisted in the database
    And the product can be retrieved via GET "/products/{id}"

  @negative @error-handling
  Scenario: Create product returns 400 when required field "name" is missing
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing field "name"
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"
    And the response body contains field-level error message for "name"

  @negative @error-handling
  Scenario: Create product returns 400 when required field "description" is missing
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing field "description"
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"
    And the response body contains field-level error message for "description"

  @negative @error-handling
  Scenario: Create product returns 400 when required field "price" is missing
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing field "price"
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains field-level error message for "price"

  @negative @error-handling
  Scenario: Create product returns 400 when required field "category" is missing
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing field "category"
    Then the API responds with HTTP status 400
    And the response body contains field-level error message for "category"

  @negative @error-handling
  Scenario: Create product returns 400 when required field "stockQuantity" is missing
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing field "stockQuantity"
    Then the API responds with HTTP status 400
    And the response body contains field-level error message for "stockQuantity"

  @invalid-input @validation
  Scenario: Create product returns 400 when price is negative
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field  | value |
      | price  | -10.00 |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains error message about price validation
    And the response body contains "price must be greater than 0"

  @invalid-input @validation
  Scenario: Create product returns 400 when price is zero
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field  | value |
      | price  | 0.00  |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains error message about price validation

  @invalid-input @validation
  Scenario: Create product returns 400 when stockQuantity is negative
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field         | value |
      | stockQuantity | -5    |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains error message about stockQuantity validation
    And the response body contains "Stock quantity cannot be negative"

  @authentication @security
  Scenario: Create product returns 401 when authentication token is missing
    When the client sends a POST request to "/products"
    And the request body contains valid product data
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"
    And the response body contains message "Authentication required"

  @authorization @security
  Scenario: Create product returns 403 when user lacks PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"
    And the response body contains message "Access Denied"

  @content-type @response-format
  Scenario: Create product response Content-Type is application/json
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 201
    And the response header "Content-Type" is "application/json"

  @data-persistence @integration
  Scenario: Created product is persisted and retrievable
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client creates a new product with valid details:
      | field          | value                                 |
      | name           | Test Product                          |
      | description    | Test Description                      |
      | price          | 99.99                                 |
      | category       | Electronics                           |
      | stockQuantity  | 100                                   |
    Then the API responds with HTTP status 201
    And the response contains a generated product "id"
    When the client retrieves the product using the generated id with a GET request to "/products/{id}"
    Then the API responds with HTTP status 200
    And the retrieved product data matches the created product data


# ==================== US-003: Update an Existing Product ====================

Feature: US-003 - Update an Existing Product

  As a product manager
  I want to update the details of an existing product by its ID
  So that the product catalog always reflects the most current and accurate information

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"
    And a product with id 1 exists in the database

  @smoke @critical
  Scenario: Successfully update product with valid details
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Content-Type" with value "application/json"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field          | value                   |
      | name           | Updated Product Name    |
      | description    | Updated description     |
      | price          | 149.99                  |
      | category       | Electronics             |
      | stockQuantity  | 200                     |
    Then the API responds with HTTP status 200
    And the response header "Content-Type" is "application/json"
    And the response body contains updated product fields
    And the changes are persisted in the database

  @negative @error-handling
  Scenario: Update product returns 404 when product does not exist
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    And no product exists with id 999
    When the client sends a PUT request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 404
    And the response body contains error type "Not Found"
    And the response body contains message "Product with id 999 not found"

  @invalid-input @validation
  Scenario: Update product returns 400 for invalid (non-integer) ID
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/abc"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"

  @null-input @validation
  Scenario: Update product returns 400 when request body is empty
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is empty or null
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"

  @invalid-input @validation
  Scenario: Update product returns 400 when price is negative
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field  | value  |
      | price  | -50.00 |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains field-level validation message for "price"

  @invalid-input @validation
  Scenario: Update product returns 400 when stockQuantity is negative
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field         | value |
      | stockQuantity | -10   |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body contains field-level validation message for "stockQuantity"

  @authentication @security
  Scenario: Update product returns 401 when authentication token is missing
    When the client sends a PUT request to "/products/1"
    And the request body contains valid product data
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"

  @authorization @security
  Scenario: Update product returns 403 when user lacks PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"

  @response-validation @data-integrity
  Scenario: Updated product fields are persisted correctly
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field          | value                           |
      | name           | New Product Name                |
      | description    | New Description                 |
      | price          | 199.99                          |
      | category       | New Category                    |
      | stockQuantity  | 300                             |
    Then the API responds with HTTP status 200
    When the client retrieves the product via GET "/products/1"
    Then the response body field "name" equals "New Product Name"
    And the response body field "description" equals "New Description"
    And the response body field "price" equals 199.99
    And the response body field "category" equals "New Category"
    And the response body field "stockQuantity" equals 300


# ==================== US-004: Delete a Product by ID ====================

Feature: US-004 - Delete a Product by ID

  As a product manager
  I want to delete a product by its ID
  So that discontinued or erroneous products are removed from the catalog and no longer accessible

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"
    And a product with id 1 exists in the database

  @smoke @critical
  Scenario: Successfully delete product for valid product ID
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a DELETE request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 204
    And the response body is empty
    And the product is removed from the database

  @data-integrity @integration
  Scenario: Deleted product is no longer accessible
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    And a product with id 1 exists in the database
    When the client sends a DELETE request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 204
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 404
    And the response body contains message "Product with id 1 not found"

  @negative @error-handling
  Scenario: Delete product returns 404 when product does not exist
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    And no product exists with id 999
    When the client sends a DELETE request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 404
    And the response body contains error type "Not Found"
    And the response body contains message "Product with id 999 not found"

  @invalid-input @validation
  Scenario: Delete product returns 400 for invalid (non-integer) ID
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a DELETE request to "/products/abc"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"

  @invalid-input @validation
  Scenario: Delete product returns 400 for negative ID
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a DELETE request to "/products/-1"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"

  @null-input @validation
  Scenario: Delete product returns 400 when ID is missing from path
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a DELETE request to "/products/" with missing ID parameter
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    Then the API responds with HTTP status 400
    And the response body contains error type "Bad Request"

  @authentication @security
  Scenario: Delete product returns 401 when authentication token is missing
    When the client sends a DELETE request to "/products/1"
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"
    And the response body contains message "Authentication required"

  @authorization @security
  Scenario: Delete product returns 403 when user lacks PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a DELETE request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"


# ==================== US-006: Authentication and Authorization ====================

Feature: US-006 - Authentication and Authorization

  As a security-conscious API owner
  I want all product endpoints to require a valid authentication token
  And enforce role-based access control
  So that only authorized users can perform product management operations

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"

  @authentication @security
  Scenario: All GET endpoints require Bearer JWT token in Authorization header
    When the client sends a GET request to "/products/1"
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"

  @authentication @security
  Scenario: All POST endpoints require Bearer JWT token
    When the client sends a POST request to "/products"
    And the request body contains valid product data
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"

  @authentication @security
  Scenario: All PUT endpoints require Bearer JWT token
    When the client sends a PUT request to "/products/1"
    And the request body contains valid product data
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"

  @authentication @security
  Scenario: All DELETE endpoints require Bearer JWT token
    When the client sends a DELETE request to "/products/1"
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"

  @authorization @security
  Scenario: GET operations accessible to PRODUCT_VIEWER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    Then the API responds with HTTP status 200 or 404 depending on product existence
    And no 403 Forbidden error is returned

  @authorization @security
  Scenario: GET operations accessible to PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_manager_token>"
    Then the API responds with HTTP status 200 or 404 depending on product existence
    And no 403 Forbidden error is returned

  @authorization @security
  Scenario: POST operations require PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"

  @authorization @security
  Scenario: PUT operations require PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a PUT request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"

  @authorization @security
  Scenario: DELETE operations require PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a DELETE request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    Then the API responds with HTTP status 403
    And the response body contains error type "Forbidden"

  @authentication @security
  Scenario: Expired JWT token returns 401 Unauthorized
    Given the client has an expired Bearer JWT token
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <expired_jwt_token>"
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"
    And the response body contains message about expired token

  @authentication @security
  Scenario: Invalid JWT token returns 401 Unauthorized
    Given the client has an invalid Bearer JWT token
    When the client sends a GET request to "/products/1"
    And the request includes header "Authorization" with value "Bearer <invalid_jwt_token>"
    Then the API responds with HTTP status 401
    And the response body contains error type "Unauthorized"


# ==================== US-007: Error Handling and Validation ====================

Feature: US-007 - Error Handling and Validation

  As a client application developer
  I want all product API errors to return structured, consistent JSON error responses
  So that I can handle errors gracefully and display meaningful messages to end users

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"

  @error-handling @response-format
  Scenario: Error responses follow consistent JSON structure
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 404
    And the response body contains the following JSON structure:
      | field     | type   |
      | status    | number |
      | error     | string |
      | message   | string |
      | timestamp | string |
      | path      | string |

  @error-handling @validation
  Scenario: HTTP 400 returned for missing required fields
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing required field "name"
    Then the API responds with HTTP status 400
    And the response body error field "status" equals 400
    And the response body error field "error" equals "Bad Request"

  @error-handling @validation
  Scenario: HTTP 400 returned for invalid data types
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field | value            |
      | price | "not-a-number"   |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body error field "error" equals "Bad Request"

  @error-handling @validation
  Scenario: HTTP 400 returned for constraint violations
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body contains:
      | field         | value |
      | stockQuantity | -10   |
    And the request body contains all other required fields
    Then the API responds with HTTP status 400
    And the response body error field "error" equals "Bad Request"

  @error-handling @authentication
  Scenario: HTTP 401 returned for unauthenticated requests
    When the client sends a GET request to "/products/1"
    And the request does NOT include "Authorization" header
    Then the API responds with HTTP status 401
    And the response body error field "status" equals 401
    And the response body error field "error" equals "Unauthorized"

  @error-handling @authorization
  Scenario: HTTP 403 returned for insufficient permissions
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_viewer_token>"
    And the request body contains valid product data
    Then the API responds with HTTP status 403
    And the response body error field "status" equals 403
    And the response body error field "error" equals "Forbidden"

  @error-handling @not-found
  Scenario: HTTP 404 returned when product does not exist
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    And no product exists with id 999
    When the client sends a GET request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 404
    And the response body error field "status" equals 404
    And the response body error field "error" equals "Not Found"
    And the response body error field "message" contains "Product with id"

  @error-handling @validation
  Scenario: Field-level validation errors included in error response
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a POST request to "/products"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_manager_token>"
    And the request body is missing fields "name" and "price"
    Then the API responds with HTTP status 400
    And the response body contains validation error for field "name"
    And the response body contains validation error for field "price"

  @error-handling @security
  Scenario: Error responses do not expose stack traces
    Given the client sends a request that causes a server error
    When the request is malformed or invalid
    Then the API responds with HTTP status 500 or appropriate error code
    And the response body error field "message" is generic and user-friendly
    And the response body does NOT contain "java.lang.Exception"
    And the response body does NOT contain "at com.ecommerce"
    And the response body does NOT contain stack trace information

  @error-handling @response-format
  Scenario: Error timestamp is in ISO 8601 format
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/999"
    And the request includes header "Authorization" with value "Bearer <valid_jwt_token>"
    Then the API responds with HTTP status 404
    And the response body error field "timestamp" matches ISO 8601 format
    And the timestamp is approximately current time (within 5 seconds)


# ==================== US-008: API Documentation ====================

Feature: US-008 - API Documentation for Product Endpoints

  As a developer or API consumer
  I want comprehensive API documentation
  So that I can understand how to interact with product endpoints without reading source code

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"

  @documentation @smoke
  Scenario: Swagger UI is accessible at /swagger-ui.html
    When the client sends a GET request to "/swagger-ui.html"
    Then the API responds with HTTP status 200
    And the response contains HTML for Swagger UI
    And the Swagger documentation is loaded and interactive

  @documentation @smoke
  Scenario: OpenAPI JSON specification is available at /v3/api-docs
    When the client sends a GET request to "/v3/api-docs"
    And the request includes header "Accept" with value "application/json"
    Then the API responds with HTTP status 200
    And the response header "Content-Type" is "application/json"
    And the response contains valid OpenAPI 3.0 specification
    And the response contains all 5 product endpoints

  @documentation @completeness
  Scenario: Each endpoint is documented with HTTP method and URL
    Given the client accesses the Swagger UI documentation
    When the client reviews the product endpoints
    Then the documentation contains endpoint for GET "/products/{id}"
    And the documentation contains endpoint for POST "/products"
    And the documentation contains endpoint for PUT "/products/{id}"
    And the documentation contains endpoint for DELETE "/products/{id}"
    And the documentation contains endpoint for GET "/products"

  @documentation @completeness
  Scenario: Each endpoint documents request parameters and body schema
    Given the client accesses the OpenAPI specification
    When the client reviews the endpoint documentation
    Then GET "/products/{id}" documents path parameter "id"
    And POST "/products" documents request body schema
    And PUT "/products/{id}" documents path parameter "id" and request body schema
    And DELETE "/products/{id}" documents path parameter "id"

  @documentation @completeness
  Scenario: Each endpoint documents response body schema
    Given the client accesses the Swagger UI
    When the client reviews response schemas for each endpoint
    Then the documentation shows response body contains fields: id, name, description, price, category, stockQuantity
    And each field shows its data type (integer, string, number)
    And array responses show item schema

  @documentation @completeness
  Scenario: Each endpoint documents possible HTTP status codes
    Given the client accesses the OpenAPI specification
    When the client reviews status codes for GET "/products/{id}"
    Then the documentation includes HTTP 200 OK
    And the documentation includes HTTP 400 Bad Request
    And the documentation includes HTTP 401 Unauthorized
    And the documentation includes HTTP 403 Forbidden
    And the documentation includes HTTP 404 Not Found

  @documentation @completeness
  Scenario: Sample request and response payloads are provided
    Given the client accesses the Swagger UI
    When the client views the POST "/products" endpoint
    Then the documentation provides sample request body JSON
    And the documentation provides sample response body JSON (HTTP 201)
    And the documentation provides sample error response JSON (HTTP 400)

  @documentation @security
  Scenario: Authentication requirements are documented
    Given the client accesses the OpenAPI specification
    When the client reviews security requirements
    Then the documentation specifies Bearer JWT authentication
    And the documentation indicates "Authorization" header is required
    And the documentation specifies Bearer token format
    And the documentation shows security is applied to all endpoints

  @documentation @security
  Scenario: Role-based access control is documented
    Given the client accesses the Swagger UI
    When the client reviews the endpoint documentation
    Then the documentation indicates GET requires "PRODUCT_VIEWER" or "PRODUCT_MANAGER"
    And the documentation indicates POST requires "PRODUCT_MANAGER"
    And the documentation indicates PUT requires "PRODUCT_MANAGER"
    And the documentation indicates DELETE requires "PRODUCT_MANAGER"

  @documentation @maintenance
  Scenario: Documentation can be updated with changes to endpoint contracts
    Given the API contract for POST "/products" is changed
    And the request body schema is updated
    When the API documentation is regenerated
    Then the Swagger UI reflects the updated schema
    And the OpenAPI JSON specification contains updated information
    And the documentation remains accurate and in sync with API


# End of BDD Test Cases Feature File
