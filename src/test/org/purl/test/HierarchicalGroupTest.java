package org.purl.test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class HierarchicalGroupTest extends AbstractIntegrationTest {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (isLoggedIn()) {
            assertLogoutUser();
        }

    }

    public void testCreatePurlWith1TierGroupWrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup1");


        assertLoginUser("testuser2", "passWord!");
        assertPurlNotCreated("/hierarchdomain1/test1TierPURL", formParameters);
    }

    public void testCreatePurlWith1TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup1");

        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertPurlCreated("/hierarchdomain1/test1TierPURL", formParameters);
    }

    public void testCreatePurlWith2TierGroupWrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup2");

        assertLoginUser("testuser2", "passWord!");
        assertPurlNotCreated("/hierarchdomain2/test2TierPURL", formParameters);
    }

    public void testCreatePurlWith2TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup2");

        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertPurlCreated("/hierarchdomain2/test2TierPURL", formParameters);
    }

    public void testCreatePurlWith3TierGroupWrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup3");

        assertLoginUser("testuser2", "passWord!");
        assertPurlNotCreated("/hierarchdomain3/test3TierPURL", formParameters);
    }

    public void testCreatePurlWith3TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup3");

        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertPurlCreated("/hierarchdomain3/test3TierPURL", formParameters);

    }

    public void testModifyPurlWith1TierGroupWrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup1");

        assertLoginUser("testuser2", "passWord!");
        assertPurlNotModifiedNotAllowed("/hierarchdomain1/test1TierPURL", formParameters);
    }

    public void testModifyPurlWith1TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup1");

        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertPurlModified("/hierarchdomain1/test1TierPURL", formParameters);
    }

    public void testModifyPurlWith2TierGroupWrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup2");

        assertLoginUser("testuser2", "passWord!");

        assertPurlNotModifiedNotAllowed("/hierarchdomain2/test2TierPURL", formParameters);
    }

    public void testModifyPurlWith2TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup2");
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");

        assertPurlModified("/hierarchdomain2/test2TierPURL", formParameters);
    }

    public void testModifyPurlWith3TierGroupwrongUser() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup3");

        assertLoginUser("testuser2", "passWord!");

        assertPurlNotModifiedNotAllowed("/hierarchdomain3/test3TierPURL", formParameters);
    }

    public void testModifyPurlWith3TierGroup() throws Exception {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://cnn.com/");
        formParameters.put("maintainers", "hierarchgroup3");

        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertPurlModified("/hierarchdomain3/test3TierPURL", formParameters);
    }

    public void testModifyDomainWith1TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying domain with 1-tier group",
                "Not allowed to update: /hierarchdomain1",
                modifyDomain("/hierarchdomain1", "domain modified", "hierarchgroup1", "hierarchgroup1", "false"));
    }

    public void testModifyDomainWith1TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertEquals("Failed modifying domain with 1-tier group",
                "Updated resource: /hierarchdomain1",
                modifyDomain("/hierarchdomain1", "domain modified", "hierarchgroup1", "hierarchgroup1", "false"));
    }


    public void testModifyDomainWith2TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying domain with 2-tier group",
                "Not allowed to update: /hierarchdomain2",
                modifyDomain("/hierarchdomain2", "domain modified", "hierarchgroup2", "hierarchgroup2", "false"));
    }


    public void testModifyDomainWith2TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertEquals("Failed modifying domain with 2-tier group",
                "Updated resource: /hierarchdomain2",
                modifyDomain("/hierarchdomain2", "domain modified", "hierarchgroup2", "hierarchgroup2", "false"));
    }

    public void testModifyDomainWith3TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying domain with 3-tier group",
                "Not allowed to update: /hierarchdomain3",
                modifyDomain("/hierarchdomain3", "domain modified", "hierarchgroup3", "hierarchgroup3", "false"));
    }

    public void testModifyDomainWith3TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertEquals("Failed modifying domain with 3-tier group",
                "Updated resource: /hierarchdomain3",
                modifyDomain("/hierarchdomain3", "domain modified", "hierarchgroup3", "hierarchgroup3", "false"));
    }

    public void testModify3TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying group with 3-tier group",
                "Not allowed to update: hierarchgroup3",
                modifyGroup("hierarchgroup3", "group modified", "hierarchgroup2", "hierarchgroup2", "group modified"));

    }

    public void testModify3TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertEquals("Failed modifying group with 3-tier group",
                "Updated resource: hierarchgroup3",
                modifyGroup("hierarchgroup3", "group modified", "hierarchgroup2", "hierarchgroup2", "group modified"));
    }

    public void testModify2TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying group with 2-tier group",
                "Not allowed to update: hierarchgroup2",
                modifyGroup("hierarchgroup2", "group modified", "hierarchgroup1", "hierarchgroup1", "group modified"));
    }

    public void testModify2TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
         assertEquals("Failed modifying group with 2-tier group",
                "Updated resource: hierarchgroup2",
                modifyGroup("hierarchgroup2", "group modified", "hierarchgroup1", "hierarchgroup1", "group modified"));
    }

    public void testModify1TierGroupWrongUser() throws Exception {

        assertLoginUser("testuser2", "passWord!");
        assertEquals("Failed modifying group with 1-tier group",
                "Not allowed to update: hierarchgroup1",
                modifyGroup("hierarchgroup1", "group modified", "testuser", "testuser", "group modified"));
    }

    public void testModify1TierGroup() throws Exception {
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertEquals("Failed modifying group with 1-tier group",
                "Updated resource: hierarchgroup1",
                modifyGroup("hierarchgroup1", "group modified", "testuser", "testuser", "group modified"));

    }


}
