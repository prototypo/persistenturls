package org.purl.test;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SinglePurlTest extends AbstractPurlTest {
    /**
     * *************** Test Single PURLs *************************
     */

    // Test creating a new PURL via an HTTP POST.
    public void testCreatePurl() {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "testuser");

        String control = "<purl status=\"1\">" +
                "<id>/testdomain/testPURL</id>" +
                "<type>302</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://cnn.com/</url></target></purl>";

        createPurl("/testdomain/testPURL", formParameters, control, true);
    }

    // Test re-creating a new PURL via an HTTP POST (should fail because it already exists).
    public void testRecreatePurl() {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "testuser");

        String control = "PURL: /testdomain/testPURL already exists.";

        createPurl("/testdomain/testPURL", formParameters, control, false);
    }

    // Test modifying an existing PURL via an HTTP PUT.
    public void testModifyPurl() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/testdomain/testPURL";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("type", "302");
            formParameters.put("target", "http://bbc.co.uk/");
            formParameters.put("maintainers", "testuser");

            String errMsg = "Cannot modify a PURL: ";
            String control = "Updated resource: /testdomain/testPURL";
            String test = client.modifyPurl(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


        // Test modifying an existing PURL via an HTTP PUT.
    public void testModifyPurlAddGroupAsMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/testdomain/testPURL";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("type", "302");
            formParameters.put("target", "http://bbc.co.uk/");
            formParameters.put("maintainers", "testuser,testgroup");

            String errMsg = "Cannot modify a PURL: ";
            String control = "Updated resource: /testdomain/testPURL";
            String test = client.modifyPurl(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test validating an existing PURL via an HTTP GET.
    public void testValidatePurl() {

        try {
            String url = "http://" + host + ":" + port + "/admin/targeturl/testdomain/testPURL";
            String result = client.validatePurl(url);

            XMLAssert.assertXpathExists("/purl[id='/testdomain/testPURL']", result);
            XMLAssert.assertXpathExists("/purl[@validation='success']", result);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }
    // Test validating a non existant PURL
    public void testValidateNonExistentPurl() {
        try {
            String url = "http://" + host + ":" + port + "/admin/targeturl/nosuchdomain/nosuchurl";
            String result = client.validatePurl(url);
            
            XMLAssert.assertXpathExists("/purl[id='/nosuchdomain/nosuchurl']", result);
            XMLAssert.assertXpathExists("/purl[@validation='failure']", result);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test resolving an existing PURL via an HTTP GET.
    public void testResolvePurl() {
        resolvePurl("/testdomain/testPURL", "http://bbc.co.uk/");
    }

    public void testCreateAndDeletePurlInPublicDomain() {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "testuser");

        String control = "<purl status=\"1\">" +
                "<id>/net/testPURL</id>" +
                "<type>302</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://cnn.com/</url></target></purl>";

        createPurl("/net/testPURL", formParameters, control, true);
        deletePurl("/net/testPURL", true);
    }

}
