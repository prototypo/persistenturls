package org.purl.test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ModifyUserTest extends AbstractIntegrationTest {

    // Test modifying an existing user via an HTTP PUT.
    public void testModifyUser() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/testuser";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test User Modified");
            formParameters.put("affiliation", "Zepheira, LLC");
            formParameters.put("email", "tuser@example.com");
            formParameters.put("passwd", "TestingAgain");
            formParameters.put("hint", "We are still testing.");
            formParameters.put("justification", "Because we like unit tests.");

            String errMsg = "Cannot modify a User: ";
            String control = "Updated resource: testuser";
            String test = client.modifyUser(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

        // Test modifying an existing user via an HTTP PUT.
    public void testModifyUserRestorePassword() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/testuser";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test User Modified");
            formParameters.put("affiliation", "Zepheira, LLC");
            formParameters.put("email", "tuser@example.com");
            formParameters.put("passwd", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
            formParameters.put("hint", "We are still testing.");
            formParameters.put("justification", "Because we like unit tests.");

            String errMsg = "Cannot modify a User: ";
            String control = "Updated resource: testuser";
            String test = client.modifyUser(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }
            // Test modifying an existing user via an HTTP PUT.
    public void testModifyUserUpperCaseUserName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/TESTUSER";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test User Modified");
            formParameters.put("affiliation", "Zepheira, LLC");
            formParameters.put("email", "tuser@example.com");
            formParameters.put("passwd", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
            formParameters.put("hint", "We are still testing.");
            formParameters.put("justification", "Because we like unit tests.");

            String errMsg = "Cannot modify a User: ";
            String control = "Updated resource: TESTUSER";
            String test = client.modifyUser(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }
}
