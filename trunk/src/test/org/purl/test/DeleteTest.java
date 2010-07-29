package org.purl.test;

/**
 *
 */
public class DeleteTest extends AbstractIntegrationTest {


    // Test deleting an existing PURL via an HTTP DELETE.
    public void testDeletePurl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/testPURL");
    }


    // Test deleting an existing 301 PURL via an HTTP DELETE.
    public void testDelete301Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test301PURL");
    }

    // Test deleting an existing 302 PURL via an HTTP DELETE.
    public void testDelete302Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test302PURL");
    }


    // Test deleting an existing 302 PURL via an HTTP DELETE.
    public void testDelete303Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test303PURL");
    }

    // Test deleting an existing 307 PURL via an HTTP DELETE.
    public void testDelete307Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test307PURL");
    }

    // Test deleting an existing 404 PURL via an HTTP DELETE.
    public void testDelete404Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test404PURL");
    }

    // Test deleting an existing 410 PURL via an HTTP DELETE.
    public void testDelete410Purl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/test410PURL");
    }

    // Test deleting an existing Clone PURL via an HTTP DELETE.
    public void testDeleteClonePurl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/testClonePURL");
    }

    // Test deleting an existing Chain PURL via an HTTP DELETE.
    public void testDeleteChainPurl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/testChainPURL");
    }

    // Test deleting an existing Partial PURL via an HTTP DELETE.
    public void testDeletePartialPurl() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        deletePurl("/testdomain/testPartialPURL");
    }

    // Test deleting an existing group via an HTTP DELETE.
    public void testDeleteGroup() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");

        try {
            String url = "http://" + host + ":" + port + "/admin/group/testgroup";

            String errMsg = "Cannot create a new group.";
            String control = "Deleted resource: testgroup";
            String test = client.deleteGroup(url);

            // Textual response, so use assertEquals().
            assertEquals(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test deleting an existing domain via an HTTP DELETE.
    public void testDeleteDomain() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            String errMsg = "Cannot delete domain.";
            String control = "Deleted resource: /testdomain";
            String test = client.deleteDomain(url);

            // Textual response, so use assertEquals().
            assertEquals(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    /**
     * *************** Utility Methods *************************
     */


    // Convenience method for deleting PURLs which are expected to succeed.
    public void deletePurl(String path) {
        deletePurl(path, true);
    }


}
