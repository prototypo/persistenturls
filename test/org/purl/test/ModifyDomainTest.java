package org.purl.test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ModifyDomainTest extends AbstractIntegrationTest {


    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomain() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain Modified");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainAddMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain Modified");
            formParameters.put("maintainers", "testuser,testuser2");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainRemoveMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain Modified");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainRemoveGroupAsMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain3";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 3 Modified");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain3";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainAddWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain Modified");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser,testuser2");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainRemoveWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain Modified");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainRemoveGroupAsWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain5";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 5 Modified");
            formParameters.put("maintainers", "testuser2");
            formParameters.put("writers", "testuser2");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain5";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainAddGroupAsWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain7";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 7 Modified");
            formParameters.put("maintainers", "testuser2");
            formParameters.put("writers", "testuser,testgroup");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain7";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test modifying an existing domain via an HTTP PUT.
    public void testModifyDomainAddGroupAsMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain7";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 7 Modified");
            formParameters.put("maintainers", "testuser2,testgroup");
            formParameters.put("writers", "testuser2,testgroup");
            formParameters.put("public", "true");

            String errMsg = "Cannot modify a Domain: ";
            String control = "Updated resource: /testdomain7";
            String test = client.modifyDomain(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }





}
