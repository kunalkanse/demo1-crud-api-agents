package com.ecommerce.product.hooks;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

/**
 * Cucumber Hooks for setting up test preconditions and postconditions.
 * Handles setup and teardown of test fixtures for API testing.
 */
public class Hooks {

    /**
     * Setup method executed before each scenario.
     * Initializes RestAssured base URI for API testing.
     */
    @Before
    public void setUp() {
        // Set the base URI for the Product API
        // This should be changed to match the environment where the API is running
        RestAssured.baseURI = "https://api.ecommerce-store.com";
        
        // Enable request/response logging for debugging
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
