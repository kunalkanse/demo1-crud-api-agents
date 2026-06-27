# Product CRUD API - Retrieve Product Details by ID (US-001)

Feature: US-001 - Retrieve Product Details by ID

  As a client application
  I want to send a GET request to "/products/{id}"
  So that I can fetch and display accurate product information for a specific product

  Background:
    Given the API base URL is "https://api.ecommerce-store.com"

# ==================== Positive Test Scenarios ====================

  @smoke @critical
  Scenario: Successfully retrieve product details for valid product ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1" with authorization
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

  @response-validation @data-integrity
  Scenario: Response includes all required product fields with correct data types
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1" with authorization
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
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 200
    And the response body field "id" equals 1
    And the response body field "name" equals "Wireless Bluetooth Headphones"
    And the response body field "description" equals "Over-ear noise-cancelling headphones with 30-hour battery life"
    And the response body field "price" equals 89.99
    And the response body field "category" equals "Electronics"
    And the response body field "stockQuantity" equals 150

  @content-type @response-format
  Scenario: Response Content-Type header is application/json
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 200
    And the response header "Content-Type" should be exactly "application/json"
    And the response body is valid JSON

  @authorization @security
  Scenario: Retrieve product succeeds with PRODUCT_MANAGER role
    Given the client has a valid Bearer JWT token with "PRODUCT_MANAGER" role
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 200
    And the response body contains the product details
    And the response body field "id" equals 1
    And the response body field "name" equals "Wireless Bluetooth Headphones"

# ==================== Negative Test Scenarios ====================

  @negative @error-handling
  Scenario: Retrieve product returns 404 Not Found for non-existent product ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    And no product exists with id 999
    When the client sends a GET request to "/products/999" with authorization
    Then the API responds with HTTP status 404
    And the response header "Content-Type" is "application/json"
    And the response body contains the following error response:
      | field   | value                          |
      | status  | 404                            |
      | error   | Not Found                      |
      | message | Product with id 999 not found  |

  @invalid-input @validation
  Scenario: Retrieve product returns 400 Bad Request for invalid non-integer ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/abc" with authorization
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"
    And the response body contains a message about invalid ID format

  @invalid-input @validation
  Scenario: Retrieve product returns 400 Bad Request for negative ID
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/-1" with authorization
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"

  @null-input @validation
  Scenario: Retrieve product returns 400 Bad Request when ID is missing from path
    Given the client has a valid Bearer JWT token with "PRODUCT_VIEWER" role
    When the client sends a GET request to "/products/" with missing ID parameter
    Then the API responds with HTTP status 400
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Bad Request"

# ==================== Authentication Test Scenarios ====================

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
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 401
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Unauthorized"

  @authentication @security
  Scenario: Retrieve product returns 401 Unauthorized when token is expired
    Given the client has an expired Bearer JWT token
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 401
    And the response header "Content-Type" is "application/json"
    And the response body contains error type "Unauthorized"

# ==================== Authorization Test Scenarios ====================

  @authorization @security
  Scenario: Retrieve product returns 403 Forbidden when user lacks required role
    Given the client has a valid Bearer JWT token with insufficient permissions
    And the user does NOT have "PRODUCT_VIEWER" or "PRODUCT_MANAGER" role
    When the client sends a GET request to "/products/1" with authorization
    Then the API responds with HTTP status 403
    And the response header "Content-Type" is "application/json"
    And the response body contains the following error response:
      | field   | value              |
      | status  | 403                |
      | error   | Forbidden          |
      | message | Access Denied      |

# End of US-001 Feature File
