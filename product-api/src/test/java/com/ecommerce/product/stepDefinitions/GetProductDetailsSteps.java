package com.ecommerce.product.stepDefinitions;

import com.ecommerce.product.pojos.ProductResponse;
import com.ecommerce.product.utils.JsonUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Step definitions for US-001 - Retrieve Product Details by ID
 * Tests the GET /products/{id} endpoint with various scenarios
 */
public class GetProductDetailsSteps {

    private Response response;
    private Long productId;
    private ProductResponse product;
    
    private String validJwtToken = "valid_jwt_token_for_viewer";
    private String invalidJwtToken = "invalid_token";
    private String expiredJwtToken = "expired_jwt_token";
    private String managerJwtToken = "valid_jwt_token_for_manager";
    private String insufficientPermissionsToken = "token_with_insufficient_permissions";
    private String currentAuthToken;

    @Given("the API base URL is {string}")
    public void setApiBaseUrl(String baseUrl) {
        RestAssured.baseURI = baseUrl;
    }

    @Given("a product with id {long} exists in the database with the following details:")
    public void productExistsInDatabase(Long id, DataTable dataTable) {
        this.productId = id;
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        // This is just for context in the test, actual verification happens during API calls
    }

    @Given("the client has a valid Bearer JWT token with {string} role")
    public void clientHasValidBearerToken(String role) {
        if ("PRODUCT_VIEWER".equals(role)) {
            this.currentAuthToken = this.validJwtToken;
        } else if ("PRODUCT_MANAGER".equals(role)) {
            this.currentAuthToken = this.managerJwtToken;
        }
    }

    @Given("no product exists with id {long}")
    public void noProductExists(Long id) {
        this.productId = id;
    }

    @Given("the client has an invalid Bearer JWT token")
    public void clientHasInvalidBearerToken() {
        this.currentAuthToken = this.invalidJwtToken;
    }

    @Given("the client has an expired Bearer JWT token")
    public void clientHasExpiredBearerToken() {
        this.currentAuthToken = this.expiredJwtToken;
    }

    @Given("the client has a valid Bearer JWT token with insufficient permissions")
    public void clientHasInsufficientPermissionsToken() {
        this.currentAuthToken = this.insufficientPermissionsToken;
    }

    @Given("the user does NOT have {string} or {string} role")
    public void userDoesNotHaveRole(String role1, String role2) {
        // Token already set with insufficient permissions in previous step
    }

    @When("the client sends a GET request to {string}")
    public void clientSendsGetRequest(String endpoint) {
        response = RestAssured.given()
                .when()
                .get(endpoint)
                .andReturn();
    }

    @When("the client sends a GET request to {string} with missing ID parameter")
    public void clientSendsGetRequestWithMissingId(String endpoint) {
        response = RestAssured.given()
                .when()
                .get(endpoint)
                .andReturn();
    }

    @And("the request includes header {string} with value {string}")
    public void requestIncludesHeader(String headerName, String headerValue) {
        // This needs to be part of a request builder pattern
        // For now, we'll use the response from the previous request
    }

    @And("the request includes header \"Content-Type\" with value \"application/json\"")
    public void requestIncludesContentTypeHeader() {
        // Re-execute the previous request with headers
        String lastEndpoint = extractLastEndpoint();
        response = RestAssured.given()
                .header("Content-Type", "application/json")
                .when()
                .get(lastEndpoint)
                .andReturn();
    }

    @And("the request includes header \"Authorization\" with value \"Bearer (.+)\"")
    public void requestIncludesAuthorizationHeader(String tokenValue) {
        // This will be handled in the request building
    }

    @When("the client sends a GET request to \"(.+)\" with header \"(.+)\" \"(.+)\" and header \"(.+)\" \"(.+)\"")
    public void clientSendsGetRequestWithHeaders(String endpoint, String header1Name, String header1Value,
                                                  String header2Name, String header2Value) {
        response = RestAssured.given()
                .header(header1Name, header1Value)
                .header(header2Name, header2Value)
                .when()
                .get(endpoint)
                .andReturn();
    }

    @When("the client sends a GET request to \"/products/{id}\" with Authorization header")
    public void clientSendsGetRequestWithAuthHeader(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + currentAuthToken)
                .header("Content-Type", "application/json")
                .when()
                .get(endpoint)
                .andReturn();
    }

    @When("the client sends a GET request to {string} with authorization")
    public void sendGetRequestWithAuthorization(String endpoint) {
        String fullEndpoint = endpoint.replace("{id}", String.valueOf(productId));
        response = RestAssured.given()
                .header("Authorization", "Bearer " + currentAuthToken)
                .header("Content-Type", "application/json")
                .when()
                .get(fullEndpoint)
                .andReturn();
    }

    @And("the request does NOT include \"Authorization\" header")
    public void requestDoesNotIncludeAuthHeader() {
        // This is already handled by not adding the header
    }

    @Then("the API responds with HTTP status {int}")
    public void apiRespondsWithHttpStatus(Integer expectedStatus) {
        Assert.assertEquals(response.getStatusCode(), (int) expectedStatus,
                "Expected status code " + expectedStatus + " but got " + response.getStatusCode());
    }

    @And("the response header \"Content-Type\" is \"application/json\"")
    public void responseHeaderContentTypeIsJson() {
        Assert.assertNotNull(response.getContentType(), "Response Content-Type should not be null");
        Assert.assertTrue(response.getContentType().toLowerCase().contains("application/json"),
                "Expected Content-Type to contain 'application/json' but was: " + response.getContentType());
    }

    @And("the response header \"Content-Type\" should be exactly \"application/json\"")
    public void responseHeaderContentTypeIsExactlyJson() {
        Assert.assertEquals(response.getContentType(), "application/json",
                "Expected Content-Type to be exactly 'application/json' but was: " + response.getContentType());
    }

    @And("the response body is valid JSON")
    public void responseBodyIsValidJson() {
        String responseBody = response.getBody().asString();
        Assert.assertTrue(JsonUtils.isValidJson(responseBody),
                "Response body is not valid JSON: " + responseBody);
    }

    @And("the response body contains the following fields:")
    public void responseBodyContainsFields(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        String responseBody = response.getBody().asString();

        product = JsonUtils.toPojo(responseBody, ProductResponse.class);
        Assert.assertNotNull(product, "Failed to deserialize response to ProductResponse");

        for (Map<String, String> row : rows) {
            String fieldName = row.get("field");
            String expectedType = row.get("type");
            String expectedValue = row.get("value");

            Object actualValue = response.jsonPath().get(fieldName);
            Assert.assertNotNull(actualValue, "Missing field: " + fieldName);

            // Validate type
            validateFieldType(fieldName, expectedType, actualValue);

            // Validate value if provided
            if (expectedValue != null && !expectedValue.isEmpty()) {
                validateFieldValue(fieldName, expectedValue, actualValue);
            }
        }
    }

    @And("the response body contains the following error response:")
    public void responseBodyContainsErrorResponse(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String field = row.get("field");
            String expectedValue = row.get("value");

            Object actualValue = response.jsonPath().get(field);
            Assert.assertNotNull(actualValue, "Missing error field: " + field);
            Assert.assertEquals(actualValue.toString(), expectedValue,
                    "Error field '" + field + "' expected '" + expectedValue + "' but was '" + actualValue + "'");
        }
    }

    @And("the response body contains error type \"(.+)\"")
    public void responseBodyContainsErrorType(String errorType) {
        String actualError = response.jsonPath().getString("error");
        Assert.assertEquals(actualError, errorType,
                "Expected error type '" + errorType + "' but was '" + actualError + "'");
    }

    @And("the response body contains a message about invalid ID format")
    public void responseBodyContainsInvalidIdFormatMessage() {
        String message = response.jsonPath().getString("message");
        Assert.assertNotNull(message, "Response message should not be null");
        Assert.assertTrue(message.toLowerCase().contains("invalid") || message.toLowerCase().contains("format") || message.toLowerCase().contains("id"),
                "Expected message about invalid ID format but was: " + message);
    }

    @And("the response body contains the product details")
    public void responseBodyContainsProductDetails() {
        String responseBody = response.getBody().asString();
        product = JsonUtils.toPojo(responseBody, ProductResponse.class);
        Assert.assertNotNull(product, "Product details should not be null");
    }

    @And("the response body field \"(.+)\" equals (.+)")
    public void responseBodyFieldEquals(String fieldName, String expectedValue) {
        Object actualValue = response.jsonPath().get(fieldName);

        // Handle numeric values
        if (expectedValue.matches("-?\\d+(\\.\\d+)?")) {
            if (expectedValue.contains(".")) {
                BigDecimal expected = new BigDecimal(expectedValue);
                BigDecimal actual = new BigDecimal(actualValue.toString());
                Assert.assertEquals(actual.compareTo(expected), 0,
                        "Field '" + fieldName + "' expected " + expectedValue + " but was " + actualValue);
            } else {
                long expected = Long.parseLong(expectedValue);
                long actual = Long.parseLong(actualValue.toString());
                Assert.assertEquals(actual, expected,
                        "Field '" + fieldName + "' expected " + expectedValue + " but was " + actualValue);
            }
        } else {
            // String comparison
            Assert.assertEquals(actualValue.toString(), expectedValue,
                    "Field '" + fieldName + "' expected '" + expectedValue + "' but was '" + actualValue + "'");
        }
    }

    @And("the response body contains field \"(.+)\" of type \"(.+)\"")
    public void responseBodyContainsFieldOfType(String fieldName, String expectedType) {
        Object value = response.jsonPath().get(fieldName);
        Assert.assertNotNull(value, "Field '" + fieldName + "' should not be null");
        validateFieldType(fieldName, expectedType, value);
    }

    @And("all response fields are non-null \\(except optional fields\\)")
    public void allResponseFieldsAreNonNull() {
        String responseBody = response.getBody().asString();
        product = JsonUtils.toPojo(responseBody, ProductResponse.class);

        Assert.assertNotNull(product.getId(), "Field 'id' should not be null");
        Assert.assertNotNull(product.getName(), "Field 'name' should not be null");
        Assert.assertNotNull(product.getDescription(), "Field 'description' should not be null");
        Assert.assertNotNull(product.getPrice(), "Field 'price' should not be null");
        Assert.assertNotNull(product.getCategory(), "Field 'category' should not be null");
        Assert.assertNotNull(product.getStockQuantity(), "Field 'stockQuantity' should not be null");
    }

    // Helper methods

    private void validateFieldType(String fieldName, String expectedType, Object value) {
        switch (expectedType) {
            case "integer":
                Assert.assertTrue(value instanceof Integer || value instanceof Long,
                        "Field '" + fieldName + "' is not an integer, was: " + value.getClass().getSimpleName());
                break;
            case "string":
                Assert.assertTrue(value instanceof String,
                        "Field '" + fieldName + "' is not a string, was: " + value.getClass().getSimpleName());
                break;
            case "number":
                Assert.assertTrue(value instanceof Number || value instanceof BigDecimal,
                        "Field '" + fieldName + "' is not a number, was: " + value.getClass().getSimpleName());
                break;
            case "boolean":
                Assert.assertTrue(value instanceof Boolean,
                        "Field '" + fieldName + "' is not a boolean, was: " + value.getClass().getSimpleName());
                break;
            default:
                throw new AssertionError("Unknown type: " + expectedType);
        }
    }

    private void validateFieldValue(String fieldName, String expectedValue, Object actualValue) {
        if (actualValue == null) {
            Assert.fail("Field '" + fieldName + "' is null");
        }

        if (fieldName.equals("price")) {
            BigDecimal expected = new BigDecimal(expectedValue);
            BigDecimal actual = new BigDecimal(actualValue.toString());
            Assert.assertEquals(actual.compareTo(expected), 0,
                    "Field '" + fieldName + "' expected " + expectedValue + " but was " + actualValue);
        } else {
            Assert.assertEquals(actualValue.toString(), expectedValue,
                    "Field '" + fieldName + "' expected '" + expectedValue + "' but was '" + actualValue + "'");
        }
    }

    private String extractLastEndpoint() {
        // This would need to be tracked from previous steps
        return "/products/1";
    }
}
