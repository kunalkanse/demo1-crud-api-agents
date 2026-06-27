package com.ecommerce.product.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Test Runner class for Cucumber tests
 * Uses TestNG to run Cucumber feature files and step definitions
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.ecommerce.product.stepDefinitions", "com.ecommerce.product.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json"
        },
        monochrome = true,
        publish = false
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
    // This class runs all scenarios in the features folder with TestNG
}
