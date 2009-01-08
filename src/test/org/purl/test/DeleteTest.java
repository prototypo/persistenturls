package org.purl.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DeleteTest extends AbstractIntegrationTest {

    // Test deleting an existing PURL via an HTTP DELETE.
    public void testDeletePurl() {
        deletePurl("/testdomain/testPURL");
    }
    

	// Test deleting an existing 301 PURL via an HTTP DELETE.
	public void testDelete301Purl() {
		deletePurl("/testdomain/test301PURL");
	}

    	// Test deleting an existing 302 PURL via an HTTP DELETE.
	public void testDelete302Purl() {
		deletePurl("/testdomain/test302PURL");
	}


	// Test deleting an existing 302 PURL via an HTTP DELETE.
	public void testDelete303Purl() {
		deletePurl("/testdomain/test303PURL");
	}

    	// Test deleting an existing 307 PURL via an HTTP DELETE.
	public void testDelete307Purl() {
		deletePurl("/testdomain/test307PURL");
	}

    	// Test deleting an existing 404 PURL via an HTTP DELETE.
	public void testDelete404Purl() {
		deletePurl("/testdomain/test404PURL");
	}

    // Test deleting an existing 410 PURL via an HTTP DELETE.
    public void testDelete410Purl() {
        deletePurl("/testdomain/test410PURL");
    }
    	// Test deleting an existing Clone PURL via an HTTP DELETE.
	public void testDeleteClonePurl() {
		deletePurl("/testdomain/testClonePURL");
	}
    	// Test deleting an existing Chain PURL via an HTTP DELETE.
	public void testDeleteChainPurl() {
		deletePurl("/testdomain/testChainPURL");
	}

    	// Test deleting an existing Partial PURL via an HTTP DELETE.
	public void testDeletePartialPurl() {
		deletePurl("/testdomain/testPartialPURL");
	}

    // Test deleting an existing group via an HTTP DELETE.
    public void testDeleteGroup() {

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
    public void testDeleteDomain() {

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

    // Test deleting an existing user via an HTTP DELETE.
    public void testDeleteUser() {
        deleteUser("testuser");
    }

    public void testDeleteUser2NoLogin() {
        // Delete user without logging in, expect failure
        deleteUser("testuser2", false, false);
    }

    // Test deleting the other user via an HTTP DELETE.
    public void testDeleteUser2() {
        // Delete user with logging in, expect success
        deleteUser("testuser2", true, true);
    }


    /****************** Utility Methods **************************/


    /**
     * Delete a user via an HTTP DELETE.
     *
     * @param uid A user id (e.g. "dwood").
     */
    public void deleteUser(String uid) {
        deleteUser(uid, false, true);
    }

    public void deleteUser(String uid, boolean loginFirst, boolean expectSuccess) {

        if (loginFirst) {
            // For now we assume we want to login as testuser2

            String url = "http://" + host + ":" + port + "/admin/login/login-submit.bsh";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("id", "testuser2");
            formParameters.put("passwd", "passWord!");
            formParameters.put("referrer", "/docs/index.html");

            try {
                client.login(url, formParameters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String test = null;
        try {
            String url = "http://" + host + ":" + port + "/admin/user/" + uid;
            test = client.deleteUser(url);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }

        String control = null;
        String errMsg = null;

        if (expectSuccess) {
            errMsg = "Cannot delete User.";
            control = "Deleted resource: " + uid;
        } else {
            errMsg = "Could (but shouldn't) delete User.";
            control = "Not allowed to delete: " + uid;
        }

        // Textual response, so use assertEquals().
        assertEquals(errMsg, control, test);
    }



    // Convenience method for deleting PURLs which are expected to succeed.
    public void deletePurl(String path) {
        deletePurl(path, true);
    }

    /** Delete a PURL via HTTP DELETE.
      * @param path A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
      * @param expectSuccess Whether the test should expect to succeed.  If false, it will expect to fail.
    */
    public void deletePurl(String path, boolean expectSuccess) {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl" + path;
            String purlName = path;//.substring(path.lastIndexOf('/') + 1, path.length() );

            String errMsg = "Cannot delete PURL.";
            String control = "Deleted resource: " + purlName;
            String test = client.deletePurl(url);

            if (expectSuccess) {
                // This test expects to succeed.
                // Textual response, so use assertEquals().
                assertEquals(errMsg, control, test);
            } else {
                // This test expects to fail.
                control = "No such resource: " + purlName;
                // Textual response, so use assertEquals().
                assertEquals(errMsg, control, test);
            }

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    

}
