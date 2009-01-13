package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CreateDomainTest extends AbstractIntegrationTest {

    /**
     * *************** Test Domains *************************
     */

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomain() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "false");

            String errMsg = "Cannot create a new domain.";
            String control = "<domain status=\"1\"><id>/testdomain</id><name>Test Domain</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
            String test = client.createDomain(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
            // TODO: DBG
            //assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    
    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleMaintainers() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain2";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 2");
            formParameters.put("maintainers", "testuser,testuser2");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "false");

            String errMsg = "Cannot create a new domain.";
            String control = "<domain status=\"1\"><id>/testdomain2</id><name>Test Domain 2</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
            String test = client.createDomain(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
            // TODO: DBG
            //assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain3";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 3");
            formParameters.put("maintainers", "testgroup, testuser");
            formParameters.put("writers", "testuser");
            formParameters.put("public", "false");

            String errMsg = "Cannot create a new domain.";
            String control = "<domain status=\"1\"><id>/testdomain3</id><name>Test Domain 3</name><maintainers><uid>testuser</uid><gid>testgroup</gid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
            String test = client.createDomain(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
            // TODO: DBG
            //assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleWriters() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain4";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 4");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testuser,testuser2");
            formParameters.put("public", "false");

            String errMsg = "Cannot create a new domain.";
            String control = "<domain status=\"1\"><id>/testdomain4</id><name>Test Domain 4</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid><uid>testuser2</uid></writers><public>false</public></domain>";
            String test = client.createDomain(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
            // TODO: DBG
            //assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain5";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", "Test Domain 5");
            formParameters.put("maintainers", "testuser");
            formParameters.put("writers", "testgroup");
            formParameters.put("public", "false");

            String errMsg = "Cannot create a new domain.";
            String control = "<domain status=\"1\"><id>/testdomain5</id><name>Test Domain 5</name><maintainers><uid>testuser</uid></maintainers><writers><gid>testgroup</gid></writers><public>false</public></domain>";
            String test = client.createDomain(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
            // TODO: DBG
            //assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        return suite;
    }

}
