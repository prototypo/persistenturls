package org.purl.test;

/**
 *
 */
public class HierarchicalGroupTest extends AbstractIntegrationTest {



    public void testCreatePurlWithHierarchicalGroups() throws Exception {
        
    }

    public void testModifyDomainWithHierarchicalGroups() throws Exception {

        assertEquals("Failed modifying domain with 1-tier group",
                modifyDomain("/hierarchdomain1", "domain modified", "testgroup6", "testgroup6", "false"),
                "Updated resource: /herarchdomain1");


        assertEquals("Failed modifying domain with 2-tier group",
                modifyDomain("/hierarchdomain2", "domain modified", "testgroup7", "testgroup7", "false"),
                "Updated resource: /herarchdomain2");


        assertEquals("Failed modifying domain with 3-tier group",
                modifyDomain("/hierarchdomain3", "domain modified", "testgroup8", "testgroup8", "false"),
                "Updated resource: /herarchdomain3");
    }

}
