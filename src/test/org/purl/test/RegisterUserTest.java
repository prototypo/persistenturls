package org.purl.test;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class RegisterUserTest extends AbstractIntegrationTest {

    // Track whether <allowUserAutoCreation/> is set or not.  The state of
    // this variable determines the expected result codes for user registration
    // and searching.
    boolean userAutoCreationOn;

    /**
     * *************** Test Users *************************
     */

    // Test registering a new user via an HTTP POST.
    public void testRegisterUser() {
        registerUser("testuser", "Test User", "Zepheira", "test.user@example.com", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"", "We are testing just now.", "Because unit tests are a necessary part of modern software development.");
    }

    // Test registering another user via an HTTP POST.
    public void testRegisterUser2() {
        registerUser("testuser2", "Another Test User", "Zepheira", "another.test.user@example.com", "passWord!", "", "");
    }

    public void testLoginUser() {
	    // Log in as testuser.
	    assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
    }
	

    /**
     * Register a new user via an HTTP POST.
     * <p/>
     * NB: Assumes that immediate user registration is available in the PURL server.
     * To turn on immediate user registration, unset <allowUserAutoCreation/>
     * in src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
     *
     * @param uid           A user id (e.g. "dwood").
     * @param name          The name of the user (e.g. "David Wood").
     * @param affiliation   The user's affiliation (such as a company name).
     * @param email         The user's email address
     * @param passwd        The user's new password (to create the account; a password is not required to authenticate yet).
     * @param hint          A hint that they user can later user to remember their password.
     * @param justification A justification as to why the account should be created (some PURL servers require this).
     */
    public void registerUser(String uid, String name, String affiliation, String email, String passwd, String hint, String justification) {

        int userAutoCreationFlag = 1;
        try {
            String url = "http://" + host + ":" + port + "/admin/user/" + uid;

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("name", name);
            formParameters.put("affiliation", affiliation);
            formParameters.put("email", email);
            formParameters.put("passwd", passwd);
            formParameters.put("hint", hint);
            formParameters.put("justification", justification);

            String errMsg = "Cannot register a new user.";
            String test = client.registerUser(url, formParameters);

            // Determine whether users may be automatically registered
            // or whether they require approval.
            if (userAutoCreationOn != true && userAutoCreationOn != false) {
                // Set userAutoCreationOn the first time it can be determined.
                if (Pattern.matches("status=\"0\"", test)) {
                    userAutoCreationOn = false;
                } else if (Pattern.matches("status=\"1\"", test)) {
                    userAutoCreationOn = true;
                } else {
                    // The status attribute was not found in the server's response.
                    // This is an error condition;
                    fail("Test registerUser() failed to understand the message from the server.");
                }
            }

            if (userAutoCreationOn) {
                userAutoCreationFlag = 0;
            }
            String control = "<user admin=\"false\" status=\"" + userAutoCreationFlag + "\"><id>" + uid + "</id><name>" + name + "</name><affiliation>" + affiliation + "</affiliation><email>" + email + "</email></user>";

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

}
