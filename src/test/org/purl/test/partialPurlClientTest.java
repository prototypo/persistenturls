package org.purl.test;

import junit.framework.TestCase;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JUnit tests for simplePurlClient.java
 *
 * @author David Hyland-Wood &lt;david@zepheira.com&gt;
 * @version $Rev$
 */
public class partialPurlClientTest extends TestCase {
    
	// Create an instance of the PURL test client for all methods to use.
	private simplePurlClient client = new simplePurlClient();
		

	/****************** Users **************************/

	
	public void testUserActions() {

		// Test registering a new user via an HTTP POST.
		try {
			String url = "http://localhost:8080/admin/user/testuser";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test User");
			formParameters.put("affiliation", "Zepheira");
			formParameters.put("email", "test.user@example.com");
			formParameters.put("passwd", "Testing!");
			formParameters.put("hint", "We are testing just now.");
			formParameters.put("justification", "Because unit tests are a necessary part of modern software development.");

			String result = client.registerUser(url, formParameters);
			assertEquals("Cannot register a new user.",
						"<user><id>testuser</id><name>Test User</name><affiliation>Zepheira</affiliation><email>test.user@example.com</email></user>",
						result);
		} catch (Exception e) {
			reportException("1.  Registering a user.  Failed to resolve URL: ", e);
		}

		// Test modifying an existing user via an HTTP PUT.
		try {
			String url = "http://localhost:8080/admin/user/testuser";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test User Modified");
			formParameters.put("affiliation", "Zepheira");
			formParameters.put("email", "test.user@example.com");
			formParameters.put("passwd", "Testing!");
			formParameters.put("hint", "We are testing just now.");
			formParameters.put("justification", "Because we like unit tests.");

			String result = client.modifyUser(url, formParameters);
			assertEquals("Cannot modify a new user.",
						"TODO",
						result);
		} catch (Exception e) {
			reportException("2.  Modifying a user.  Failed to resolve URL: ", e);
		}

		// Test searching for users via an HTTP GET.
		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String result = client.searchUser(url);
			assertEquals("Cannot search user.",
						"<user><id>testuser</id><name>Test User</name><affiliation>Zepheira</affiliation><email>test.user@example.com</email></user>",
						result);
			} catch (Exception e) {
				reportException("3.  Searching for a user.  Failed to resolve URL: ", e);
			}

			// Test deleting an existing user via an HTTP DELETE.
		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String result = client.deleteUser(url);
			assertEquals("Cannot delete User.",
						"Deleted resource: testuser",
						result);
		} catch (Exception e) {
			reportException("4.  Deleting a user.  Failed to resolve URL: ", e);
		}
	}
	

	/****************** Utility Methods **************************/
	
	// Handle any exceptions encountered above.  For JUnit tests, it
	// is enough to call JUnit's fail() method.
	private void reportException(String message, Exception e) {
		fail( message + e.getMessage() + " : " + e.getCause() );
	}
}
