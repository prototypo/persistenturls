package org.purl.test;

import org.custommonkey.xmlunit.XMLAssert;

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
        assertDomainCreated("/testdomain","Test Domain", "testuser", "testuser", false);
    }


    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleMaintainers() throws Exception {
        assertDomainCreated("/testdomain2","Test Domain 2", "testuser,testuser2", "testuser", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsMaintainer() throws Exception {        
        assertDomainCreated("/testdomain3","Test Domain 3", "testgroup,testuser", "testuser", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithMultipleWriters() throws Exception {
        assertDomainCreated("/testdomain4","Test Domain 4", "testuser", "testuser,testuser2", false);
    }

    // Test creating a new domain via an HTTP POST.
    public void testCreateDomainWithAGroupAsWriter() throws Exception {
        assertDomainCreated("/testdomain5","Test Domain 5", "testuser", "testgroup", false);
    }

    // Test creating a nested domain.  A root should be required.
    public void testCreateNestedDomains() throws Exception {
        assertDomainNotCreatedNoRoot("/testroot/testdomain6", "Test Domain 6", "testuser", "testuser", false);
        assertDomainCreated("/testroot", "Test Root Domain", "testuser", "testuser", false);
        assertDomainCreated("/testroot/testdomain6",  "Test Domain 6", "testuser", "testuser", false);
    }

    public void testCreateDuplicateDomains() throws Exception {
        assertDomainCreated("/testdomain7", "Test Domain 7", "testuser", "testuser", false);
        assertDomainNotCreatedAlreadyExists("/testdomain7", "Test Domain 7", "testuser", "testuser", false);

    }

    public void testCreateDomainWithHierarchicalGroup() throws Exception {
        assertDomainCreated("/hierarchdomain1", "", "testgroup6", "testgroup6", false);
        assertDomainCreated("/hierarchdomain2", "", "testgroup7", "testgroup7", false);
        assertDomainCreated("/hierarchdomain3", "", "testgroup8", "testgroup8", false);
    }



}
