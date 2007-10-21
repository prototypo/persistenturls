package org.purl.test;

// JUnit
import junit.framework.TestCase;

// XMLUnit
import org.custommonkey.xmlunit.XMLAssert;

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
    
	// TODO: Include test results in all error messages.
	
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

	// Test creating a new PURL via an HTTP POST.
	public void testCreatePurl() {

		try {
			String url = "http://localhost:8080/admin/purl/testdomain/testPURL";

			Map<String, String> formParameters = new HashMap<String,String> ();
			formParameters.put("type", "302");
			formParameters.put("target", "http://bbc.co.uk/");
			formParameters.put("maintainers", "testuser");

			String errMsg = "Cannot create a new PURL.";
			String control = "<purl><pid>/testdomain/testPURL</pid><type>302</type><target><url>http://bbc.co.uk/</url></target><maintainers><uid>testuser</uid></maintainers></purl>";
			String test = client.createPurl(url, formParameters);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying an existing PURL via an HTTP PUT.
	public void testModifyPurl() {

		try {
			String url = "http://localhost:8080/admin/purl/testdomain/testPURL";

			File file = new File(System.getProperty("user.dir") + 
								System.getProperty("file.separator") +
								"test" + 
								System.getProperty("file.separator") + 
								"testdata" + 
								System.getProperty("file.separator"), 
								"purlmodify.xml");

			String errMsg = "Cannot modify a PURL: ";
			String control = "Updated resource: /testdomain/testPURL";
			String test = client.modifyPurl(url, file);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for PURLs via an HTTP GET.
	public void testSearchPurl() {

		try {
			String url = "http://localhost:8080/admin/purl/testdomain/testPURL";

			String errMsg = "Cannot search PURL.  Returned message was: ";
			String control = "<purl><pid>/testdomain/testPURL</pid><type>302</type><target><url>http://bbc.co.uk/</url></target><maintainers><uid>testuser</uid></maintainers></purl>";
			String test = client.searchPurl(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg + test, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test validating an existing PURL via an HTTP GET.
	public void testValidatePurl() {

		try {
			String url = "http://localhost:8080/admin/validate/testdomain/testPURL";

			String result = client.validatePurl(url);
			assertEquals("Cannot validate PURL.",
						"TODO",
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test resolving an existing PURL via an HTTP GET.
	// TODO: Presumes a PURL type of "302".  Is that OK?  Probably not.
	public void testResolvePurl() {

		try {
			String url = "http://localhost:8080/purl/testdomain/testPURL";

			String errMsg = "Cannot resolve PURL.";
			String control = "http://bbc.co.uk/";
			String test = client.resolvePurl(url);
			
			// Textual response, so use assertEquals().
			assertEquals(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test deleting an existing PURL via an HTTP DELETE.
	public void testDeletePurl() {

		try {
			String url = "http://localhost:8080/admin/purl/testdomain/testPURL";

			String errMsg = "Cannot delete PURL.";
			String control = "Deleted resource: testPURL";
			String test = client.deletePurl(url);

			// Textual response, so use assertEquals().
			assertEquals(errMsg, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}


	/****************** Batch PURLs **************************/
	// Test creating a batch of PURLs via an HTTP POST.
	public void testCreatePurls() {

		try {
			String url = "http://localhost:8080/admin/purls/";

			File file = new File(System.getProperty("user.dir") + 
								System.getProperty("file.separator") +
								"test" + 
								System.getProperty("file.separator") + 
								"testdata" + 
								System.getProperty("file.separator"), 
								"purlscreate.xml");

			String errMsg = "Cannot create a batch of PURLs.";
			// TODO: Fix the control as soon as we know what comes back from a success!
			String control = "<purl><pid>/tld/oclc/test303/</pid></purl>";
			String test = client.createPurls(url, file);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying a batch of PURLs via an HTTP PUT.
	public void testModifyPurls() {

		try {
			String url = "http://localhost:8080/admin/purls/";

			File file = new File(System.getProperty("user.dir") + 
								System.getProperty("file.separator") +
								"test" + 
								System.getProperty("file.separator") + 
								"testdata" + 
								System.getProperty("file.separator"), 
								"purlsmodify.xml");

			String errMsg = "Cannot modify a batch of PURLs.";
			// TODO: Fix the control as soon as we know what comes back from a success!
			String control = "<purl><pid>/tld/oclc/test303/</pid></purl>";
			String test = client.modifyPurls(url, file);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}	
	
	
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

			String errMsg = "Cannot register a new user.";
			String control = "<user><id>testuser</id><name>Test User</name><affiliation>Zepheira</affiliation><email>test.user@example.com</email></user>";
			String test = client.registerUser(url, formParameters);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying an existing user via an HTTP PUT.
	public void testModifyUser() {

		try {
			String url = "http://localhost:8080/admin/user/testuser";

			File file = new File(System.getProperty("user.dir") + 
								System.getProperty("file.separator") +
								"test" + 
								System.getProperty("file.separator") + 
								"testdata" + 
								System.getProperty("file.separator"), 
								"usermodify.xml");

			String errMsg = "Cannot modify a User: ";
			String control = "Updated resource: testuser";
			String test = client.modifyUser(url, file);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUser() {

		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String errMsg = "Cannot search user.";
			String control = "<user><id>testuser</id><name>Test User</name><affiliation>Zepheira</affiliation><email>test.user@example.com</email></user>";
			//String control = "<user><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test deleting an existing user via an HTTP DELETE.
	public void testDeleteUser() {

		try {
			String url = "http://localhost:8080/admin/user/testuser";

			String errMsg = "Cannot delete User.";
			String control = "Deleted resource: testuser";
			String test = client.deleteUser(url);

			// Textual response, so use assertEquals().
			assertEquals(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	
	/****************** Groups **************************/
	
	// Test creating a new group via an HTTP POST.
	public void testCreateGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A group used for unit tests.");
							
			String errMsg = "Cannot create a new group.";
			String control = "<group><id>testgroup</id><name>Test Group</name><maintainers>testuser</maintainers><members>testuser</members><comments>A group used for unit tests.</comments></group>";
			String test = client.createGroup(url, formParameters);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroup() {

			try {
				String url = "http://localhost:8080/admin/group/testgroup";

				File file = new File(System.getProperty("user.dir") + 
									System.getProperty("file.separator") +
									"test" + 
									System.getProperty("file.separator") + 
									"testdata" + 
									System.getProperty("file.separator"), 
									"groupmodify.xml");

				String errMsg = "Cannot modify a Group: ";
				String control = "Updated resource: testgroup";
				String test = client.modifyGroup(url, file);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group</name><maintainers>testuser</maintainers><members>testuser</members><comments>A group used for unit tests.</comments></group>";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers>david</maintainers><members>brian</members><comments>Because we like lots of unit tests.</comments></group>";
			String test = client.searchGroup(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test deleting an existing group via an HTTP DELETE.
	public void testDeleteGroup() {
		
		try {
			String url = "http://localhost:8080/admin/group/testgroup";
		
			String errMsg = "Cannot create a new group.";
			String control = "Deleted resource: testgroup";
			String test = client.deleteGroup(url);
			
			// Textual response, so use assertEquals().
			assertEquals(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	
	/****************** Domains **************************/
	
	// Test creating a new domain via an HTTP POST.
	public void testCreateDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain");
			formParameters.put("maintainers", "testuser");
			formParameters.put("writers", "testuser");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>testdomain</id><name>Test Domain</name><maintainers>testuser</maintainers><writers>testuser</writers></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomain() {

			try {
				String url = "http://localhost:8080/admin/domain/testdomain";

				File file = new File(System.getProperty("user.dir") + 
									System.getProperty("file.separator") +
									"test" + 
									System.getProperty("file.separator") + 
									"testdata" + 
									System.getProperty("file.separator"), 
									"domainmodify.xml");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: testdomain";
				String test = client.modifyDomain(url, file);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>testdomain</id><name>Test Domain</name><maintainers>testuser</maintainers><writers>testuser</writers></domain>";
			//String control = "<domain><id>testdomain</id><name>Test Domain Modified</name><maintainers>david,eric</maintainers><writers>zepheira,david,brian</writers></domain>";
			String test = client.searchDomain(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg,
						control,
						test);
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test deleting an existing domain via an HTTP DELETE.
	public void testDeleteDomain() {
		
		try {
			String url = "http://localhost:8080/admin/domain/testdomain";
		
			String errMsg = "Cannot delete domain.";
			String control = "Deleted resource: testdomain";
			String test = client.deleteDomain(url);
			
			// Textual response, so use assertEquals().
			assertEquals(errMsg, control, test);
						
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
