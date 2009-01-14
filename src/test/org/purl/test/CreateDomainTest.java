package org.purl.test;

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
    public void testCreateDomain() throws Exception {
        assertCreated("/testdomain","Test Domain", "testuser", "testuser", false);
    }


    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleMaintainers() throws Exception {
        assertCreated("/testdomain2","Test Domain 2", "testuser,testuser2", "testuser", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsMaintainer() throws Exception {        
        assertCreated("/testdomain3","Test Domain 3", "testgroup,testuser", "testuser", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleWriters() throws Exception {
        assertCreated("/testdomain4","Test Domain 4", "testuser", "testuser,testuser2", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsWriter() throws Exception {
        assertCreated("/testdomain5","Test Domain 5", "testuser", "testgroup", false);
    }

    // Test creating a nested domain.  A root should be required.
    public void testCreateNestedDomains() throws Exception {
        assertNotCreatedNoRoot("/testroot/testdomain6", "Test Domain 6", "testuser", "testuser", false);
        assertCreated("/testroot", "Test Root Domain", "testuser", "testuser", false);
        assertCreated("/testroot/testdomain6",  "Test Domain 6", "testuser", "testuser", false);
    }

    public void testCreateDuplicateDomains() throws Exception {
        assertCreated("/testdomain7", "Test Domain 7", "testuser", "testuser", false);
        assertNotCreatedAlreadyExists("/testdomain7", "Test Domain 7", "testuser", "testuser", false);

    }

    public void assertCreated(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        XMLAssert.assertXpathExists("/domain[@status='1']", test);
        XMLAssert.assertXpathExists("/domain[id='" + path + "']", test);
    }

    public void assertNotCreatedAlreadyExists(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        assertTrue("Expected " + path + " to already exist.  Received message: " + test, test.endsWith("cannot be created because it already exists."));
    }

    public void assertNotCreatedNoRoot(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        assertTrue("Expected " + path + " to require root domain.  Received message: " + test, test.contains("cannot be created because the root domain"));
    }

    public String createDomain(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {

        String url = "http://" + host + ":" + port + "/admin/domain" + path;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", name);
        formParameters.put("maintainers", maintainers);
        formParameters.put("writers", writers);
        formParameters.put("public", Boolean.toString(isPublic));

        return client.createDomain(url, formParameters);

    }

}
