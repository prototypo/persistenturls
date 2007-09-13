package org.purl.test;

import junit.framework.TestCase;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JUnit tests for simplePurlClient.java
 *
 * @author David Hyland-Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public class simplePurlClientTest extends TestCase {
    
	private simplePurlClient client;
	
	// Constructor
	public simplePurlClientTest (String name) {
	    super(name);
	  }

	// Create an instance of the PURL test client for all methods to use.
	protected void setUp() {
		client = new simplePurlClient();
	}
		


	/****************** Single PURLs **************************/

	// Test registering a new PURL via an HTTP POST.
	public void testRegisterPurl() {

		try {
			String url = "http://localhost:8080/admin/purl/NET/test/testPURL";

			Map<String, String> formParameters = new HashMap<String,String> ();
			formParameters.put("type", "302");
			formParameters.put("target", "http://bbc.co.uk/");
			formParameters.put("maintainers", "david,eric,brian");

			String result = client.registerPurl(url, formParameters);
			assertEquals("Cannot register a new PURL.",
						"<purl><pid>/NET/test/testPURL</pid><type>302</type><target><url>http://bbc.co.uk/</url></target><maintainers><uid>david,eric,brian</uid></maintainers></purl>",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying an existing PURL via an HTTP PUT.
	public void testModifyPurl() {

	try {
		String url = "http://localhost:8080/admin/purl/NET/test/testPURL";

		String formParameters = "<purl>";
		formParameters += "<type>302</type>";
		formParameters += "<target><url>http://www.your.abc.net.au/</url></target>";
		formParameters += "<maintainers><uid>eric,brian</uid></maintainers>";
		formParameters += "</purl>";

		String result = client.modifyPurl(url, formParameters);
		assertEquals("Cannot modify a PURL.",
					"Updated resource: testPURL",
					result);
	} catch (Exception e) {
		reportException("Failed to resolve URL: ", e);
	}
	}

	// Test searching for PURLs via an HTTP GET.
	public void testSearchPurl() {

		try {
			String url = "http://localhost:8080/admin/purl/NET/test/testPURL";

			String result = client.searchPurl(url);
			assertEquals("Cannot search PURL.",
						"<purl><pid>/NET/test/testPURL</pid><type>302</type><target><url>http://bbc.co.uk/</url></target><maintainers><uid>david,eric,brian</uid></maintainers></purl>",
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test validating an existing PURL via an HTTP GET.
/*	TODO: Implement.
	public void testValidatePurl() {

		try {
			URL url = new URL("http://localhost:8080/admin/validate/NET/test/testPURL");

			String result = client.validatePurl(url);
			assertEquals("Cannot validate PURL.",
						"TODO",
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}
*/

	// Test resolving an existing PURL via an HTTP GET.
	public void testResolvePurl() {

		try {
			String url = "http://localhost:8080/purl/NET/test/testPURL";

			String result = client.resolvePurl(url);
			assertEquals("Cannot resolve PURL.",
						"http://www.your.abc.net.au/",
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test deleting an existing PURL via an HTTP DELETE.
	public void testDeletePurl() {

		try {
			String url = "http://localhost:8080/admin/purl/NET/test/testPURL";

			String result = client.deletePurl(url);

			assertEquals("Cannot delete PURL.",
						"Deleted resource: testPURL",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}


	/****************** Batch PURLs **************************/
/*	TODO: Implement.
	// Test registering a batch of PURLs via an HTTP POST.
	public void testRegisterPurls() {

		try {
			URL url = new URL("http://localhost:8080/admin/purl/NET/test/testPURL");

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("file", "TODO");

			String result = client.registerPurls(url, formParameters);
			assertEquals("Cannot register a batch of PURLs.",
						"TODO",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying a batch of PURLs via an HTTP PUT.
	public void testModifyPurls() {
		String result = client.modifyPurls();
		assertEquals("Cannot modify a batch of PURLs.",
					"Modify a batch of PURLs via an HTTP PUT. Not implemented yet.",
					result);
	}

	// Test deleting a batch of PURLs via an HTTP DELETE.
	// TODO: DELETE requests can't hold bodies??
	public void testDeletePurls() {
		String result = client.deletePurls();
		assertEquals("Cannot delete a batch of PURLs.",
					"Delete a batch PURLs via an HTTP DELETE. Not implemented yet.",
					result);
	}

	// Test validating a batch of PURLs via an HTTP GET.
	// TODO: Provide XML to validatePurls().
	public void testValidatePurls() {
		String result = client.validatePurls();
		assertEquals("Cannot search domain.",
					"Validate a batch PURLs via an HTTP GET. Not implemented yet.",
					result);
	}
*/
	
	
	/****************** Users **************************/

	// Test registering a new user via an HTTP POST.
	public void testRegisterUser() {

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
			
			// DBG  TODO
			System.err.println("Adding user: " + result);

			assertEquals("Cannot register a new user.",
						"<user><id>testuser</id><name>Test User</name><affiliation>Zepheira</affiliation><email>test.user@example.com</email></user>",
						result);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying an existing user via an HTTP PUT.
	public void testModifyUser() {

		try {
			// TODONEXT:  Use HTTPTracer to see what is being sent here.
			String url = "http://localhost:8080/admin/user/testuser";

			String formParameters = "<user>";
			formParameters += "<name>Test User Modified</name>";
			formParameters += "<affiliation>Zepheira LLC</affiliation>";
			formParameters += "<email>tuser@example.com</email>";
			formParameters += "<passwd>testing</passwd>";
			formParameters += "<hint>Removed the hint.</hint>";
			formParameters += "<justification>Because we still like unit tests.</justification>";
			formParameters += "</user>";

			String result = client.modifyUser(url, formParameters);
			
			// DBG  TODO
			System.err.println("Modifying user: " + result);

			assertEquals("Cannot modify a new user.",
						"Updated resource: testuser",
						result);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUser() {

		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String result = client.searchUser(url);
			
			// DBG  TODO
			System.err.println("Searching user: " + result);

			assertEquals("Cannot search user.",
						"<user><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira LLC</affiliation><email>tuser@example.com</email></user>",
						result);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test deleting an existing user via an HTTP DELETE.
	public void testDeleteUser() {

		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String result = client.deleteUser(url);
			
			// DBG  TODO
			System.err.println("Deleting user: " + result);

			assertEquals("Cannot delete User.",
						"Deleted resource: testuser",
						result);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	
	/****************** Groups **************************/
	
	// Test registering a new group via an HTTP POST.
	public void testRegisterGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group");
			formParameters.put("maintainers", "david,brian");
			formParameters.put("members", "zepheira,david,eric,brian");
			formParameters.put("comments", "A group used for unit tests.");
							
			String result = client.registerGroup(url, formParameters);
			assertEquals("Cannot register a new group.",
						"<group><id>testgroup</id><name>Test Group</name><maintainers>david,brian</maintainers><members>zepheira,david,eric,brian</members><comments>A group used for unit tests.</comments></group>",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroup() {

			try {
				String url = "http://localhost:8080/admin/group/testgroup";

				String formParameters = "<group>";
				formParameters += "<name>Test Group Modified</name>";
				formParameters += "<maintainers>david</maintainers>";
				formParameters += "<members>brian</members>";
				formParameters += "<comments>Because we like lots of unit tests.</comments>";
				formParameters += "</group>";

				String result = client.modifyGroup(url, formParameters);
				assertEquals("Cannot modify a group.",
							"Updated resource: testgroup",
							result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";

			String result = client.searchGroup(url);
			assertEquals("Cannot search group.",
						"<group><id>testgroup</id><name>Test Group Modified</name><maintainers>david</maintainers><members>brian</members><comments>Because we like lots of unit tests.</comments></group>",
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test deleting an existing group via an HTTP DELETE.
	public void testDeleteGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";
		
			String result = client.deleteGroup(url);
			assertEquals("Cannot delete group.",
						"Deleted resource: testgroup",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	
	/****************** Domains **************************/
	
	// Test registering a new domain via an HTTP POST.
	public void testRegisterDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain");
			formParameters.put("maintainers", "eric,brian");
			formParameters.put("writers", "zepheira,david,eric");
			
			String result = client.registerDomain(url, formParameters);
			assertEquals("Cannot register a new domain.",
						"<domain><id>testdomain</id><name>Test Domain</name><maintainers>eric,brian</maintainers><writers>zepheira,david,eric</writers></domain>",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomain() {

			try {
				String url = "http://localhost:8080/admin/domain/testdomain";

				String formParameters = "<domain>";
				formParameters += "<name>Test Domain Modified</name>";
				formParameters += "<maintainers>david,eric</maintainers>";
				formParameters += "<writers>zepheira,david,brian</writers>";
				formParameters += "</domain>";

				String result = client.modifyDomain(url, formParameters);
				assertEquals("Cannot modify a domain.",
							"Updated resource: testdomain",
							result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";

			String result = client.searchDomain(url);
			assertEquals("Cannot search domain.",
						"<domain><id>testdomain</id><name>Test Domain Modified</name><maintainers>david,eric</maintainers><writers>zepheira,david,brian</writers></domain>",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test deleting an existing domain via an HTTP DELETE.
	public void testDeleteDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";
		
			String result = client.deleteDomain(url);
			assertEquals("Cannot delete domain.",
						"Deleted resource: testdomain",
						result);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	
	/****************** Utility Methods **************************/
	
	// Handle any exceptions encountered above.  For JUnit tests, it
	// is enough to call JUnit's fail() method.
	private void reportException(String message, Exception e) {
		fail( message + e.getMessage() + " : " + e.getCause() );
	}
}
