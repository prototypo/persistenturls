package org.purl.test;

/**
 *
 */
public class DeleteUserTest extends AbstractIntegrationTest {
    public void testDeleteUser2AsUser1() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertNotAllowedToDeleteUser("testuser2");

    }

    // Test deleting an existing user via an HTTP DELETE.
    public void testDeleteUser() throws Exception {
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
        assertDeleteUser("testuser");

    }


    public void testDeleteUser2NoLogin() throws Exception {
        // Delete user without logging in, expect failure
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertFailDeleteUserNotLoggedIn("testuser2");
    }

    // Test deleting the other user via an HTTP DELETE.
    public void testDeleteUser2() throws Exception {
        // Delete user with logging in, expect success
        if (isLoggedIn()) {
            assertLogoutUser();
        }
        assertLoginUser("testuser2", "passWord!");
        assertDeleteUser("testuser2");
    }


    public String deleteUser(String uid) throws Exception {

        String url = "http://" + host + ":" + port + "/admin/user/" + uid;
        return client.deleteUser(url);

    }

    public void assertDeleteUser(String uid) {
        try {
            String result = deleteUser(uid);
            assertTrue(result.startsWith("Deleted resource:"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public void assertNotAllowedToDeleteUser(String uid) {
        try {
            String result = deleteUser(uid);
            assertTrue(result.contains("Not allowed to delete"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public void assertFailDeleteUserNotLoggedIn(String uid) {
        try {
            String result = deleteUser(uid);
            assertTrue(result.contains("Please log in"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }
}
