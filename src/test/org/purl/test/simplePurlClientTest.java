package org.purl.test;

// JUnit
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * JUnit tests for simplePurlClient.java
 *
 * NB: To ensure proper ordering of tests, run via purlClientTestRunner.java.
 *
 * @author David Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public class simplePurlClientTest extends TestCase {
	
	private simplePurlClient client;
	
	// Set the host and port for all subsequent URLs.
	private String host = "localhost";
	private String port = "8080";
	
	// Track whether <allowUserAutoCreation/> is set or not.  The state of
	// this variable determines the expected result codes for user registration
	// and searching.
	boolean userAutoCreationOn;
	
	// Constructor
	public simplePurlClientTest (String name) {
	    super(name);
	}

	// Create an instance of the PURL test client for all methods to use.
	protected void setUp() {
	    client = new simplePurlClient();
	}

	/****************** Test Single PURLs **************************/
	
	// Test creating a new PURL via an HTTP POST.
	public void testCreatePurl() {
		createEasyPurl("/testdomain/testPURL", "302", "testuser", "http://cnn.com/");		
	}
	
	// Test re-creating a new PURL via an HTTP POST (should fail because it already exists).
	public void testRecreatePurl() {
		createPurl("/testdomain/testPURL", "302", "testuser", "http://cnn.com/", null, false, false);		
	}

	// Test modifying an existing PURL via an HTTP PUT.
	public void testModifyPurl() {

		try {
			String url = "http://" + host + ":" + port + "/admin/purl/testdomain/testPURL";

			Map<String, String> formParameters = new HashMap<String,String> ();
			formParameters.put("type", "302");
			formParameters.put("target", "http://bbc.co.uk/");
			formParameters.put("maintainers", "testuser");

			String errMsg = "Cannot modify a PURL: ";
			String control = "Updated resource: /testdomain/testPURL";
			String test = client.modifyPurl(url, formParameters);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for PURLs via an HTTP GET.
	public void testSearchPurl() {

		try {
			String url = "http://" + host + ":" + port + "/admin/purl/testdomain/testPURL";

			String errMsg = "Cannot search PURL.  Returned message was: ";
			String control = "<purl><id>/testdomain/testPURL</id><type>302</type><target><url>http://bbc.co.uk/</url></target><maintainers><uid>testuser</uid></maintainers></purl>";
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
			String url = "http://" + host + ":" + port + "/admin/targeturl/testdomain/testPURL";

			String result = client.validatePurl(url);
						
			String control = "<purl><id>/testdomain/testPURL</id><status result=\"success\">Success</status></purl>";
			assertEquals("Cannot validate PURL.",
						control,
						result);
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test resolving an existing PURL via an HTTP GET.
	public void testResolvePurl() {
		resolvePurl("/testdomain/testPURL", "http://bbc.co.uk/");
	}

	// Test deleting an existing PURL via an HTTP DELETE.
	public void testDeletePurl() {
		deletePurl("/testdomain/testPURL");
	}
	
	/****************** Test Batch PURLs **************************/
		
	// Test creating a batch of 301 PURLs via an HTTP POST.
	public void testCreate301Purls() {
		createPurls("purlscreate301s.xml", 2);
	}
	
	// Test creating a batch of 302 PURLs via an HTTP POST.
	public void testCreate302Purls() {
		createPurls("purlscreate302s.xml", 2);
	}
	
	// Test creating a batch of 303 PURLs via an HTTP POST.
	public void testCreate303Purls() {
		createPurls("purlscreate303s.xml", 2);
	}
	
	// Test creating a batch of 307 PURLs via an HTTP POST.
	public void testCreate307Purls() {
		createPurls("purlscreate307s.xml", 2);
	}
	
	// Test creating a batch of 404 PURLs via an HTTP POST.
	public void testCreate404Purls() {
		createPurls("purlscreate404s.xml", 2);
	}
	
	// Test creating a batch of 410 PURLs via an HTTP POST.
	public void testCreate410Purls() {
		createPurls("purlscreate410s.xml", 2);
	}
	
	// Test creating a batch of Clone PURLs via an HTTP POST.
	public void testCreateClonePurls() {
		createPurls("purlscreateClones.xml", 2);
	}

	// Test creating a batch of Chain PURLs via an HTTP POST.
	public void testCreateChainPurls() {
		createPurls("purlscreateChains.xml", 2);
	}

	// Test creating a batch of Partial Redirect PURLs via an HTTP POST.
	public void testCreatePartialRedirectPurls() {
		createPurls("purlscreatePartials.xml", 2);
	}
	
	// Test creating a batch of PURLs of different types via an HTTP POST.
	public void testCreatePurls() {
		createPurls("purlscreate.xml", 9);
	}
	
	// Test modifying a batch of PURLs via an HTTP PUT.
	public void testModifyPurls() {

		try {
			String url = "http://" + host + ":" + port + "/admin/purls/";

			File file = new File(getTestDataFile("purlsmodify.xml"));

			String errMsg = "Cannot modify a batch of PURLs: ";
			// NB: Change the number below (6) if the number of PURLs defined in the input file changes.
			String control = "<purl-batch-success numCreated=\"6\"/>";
			String test = client.modifyPurls(url, file);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying a batch of PURLs via an HTTP PUT.
	public void testValidatePurls() {

		try {
			String url = "http://" + host + ":" + port + "/admin/targeturls/";
			
			File file = new File(getTestDataFile("purlsvalidate.xml"));
			String errMsg = "Cannot validate a batch of PURLs: ";
			String control = readFile(getTestDataFile("purlsvalidatecontrol.xml"));
			String test = client.validatePurls(url, file);

			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	

	/****************** Test Advanced PURLs **************************/

	// Test creating a new 301 PURL via an HTTP POST.
	public void testCreate301Purl() {
		// NB: This PURL is used for later tests as a basePURL and so much be created first.
		createEasyPurl("/testdomain/test301PURL", "301", "testuser", "http://example.com/test301PURL");
	}
	
	// Test resolving an existing 301 PURL via an HTTP GET.
	public void testResolve301Purl() {
		resolvePurl("/testdomain/test301PURL", "http://example.com/test301PURL");
	}
	
	// Test deleting an existing 301 PURL via an HTTP DELETE.
	public void testDelete301Purl() {
		deletePurl("/testdomain/test301PURL");
	}
		
	// Test creating a new 302 PURL via an HTTP POST.
	public void testCreate302Purl() {
		createEasyPurl("/testdomain/test302PURL", "302", "testuser", "http://example.com/test302PURL");
	}
	
	// Test resolving an existing 302 PURL via an HTTP GET.
	public void testResolve302Purl() {
		resolvePurl("/testdomain/test302PURL", "http://example.com/test302PURL");
	}

	// Test deleting an existing 302 PURL via an HTTP DELETE.
	public void testDelete302Purl() {
		deletePurl("/testdomain/test302PURL");
	}
	
	// Test creating a new 303 PURL via an HTTP POST.
	public void testCreate303Purl() {
		createPurl("/testdomain/test303PURL", "303", "testuser", null, "http://example.com/test303PURL", false);
	}
	
	// Test resolving an existing 303 PURL via an HTTP GET.
	public void testResolve303Purl() {
		resolvePurl("/testdomain/test303PURL", "http://example.com/test303PURL");
	}

	// Test deleting an existing 302 PURL via an HTTP DELETE.
	public void testDelete303Purl() {
		deletePurl("/testdomain/test303PURL");
	}
	
	// Test creating a new 307 PURL via an HTTP POST.
	public void testCreate307Purl() {
		createEasyPurl("/testdomain/test307PURL", "307", "testuser", "http://example.com/test307PURL");
	}
	
	// Test resolving an existing 307 PURL via an HTTP GET.
	public void testResolve307Purl() {
		resolvePurl("/testdomain/test307PURL", "http://example.com/test307PURL");
	}

	// Test deleting an existing 307 PURL via an HTTP DELETE.
	public void testDelete307Purl() {
		deletePurl("/testdomain/test307PURL");
	}
	
	// Test creating a new 404 PURL via an HTTP POST.
	public void testCreate404Purl() {
		createEasyPurl("/testdomain/test404PURL", "404", "testuser", null);
	}
	
	// Test resolving an existing 404 PURL via an HTTP GET.
	public void testResolve404Purl() {
		resolvePurl("/testdomain/test404PURL", "Not Found");
	}

	// Test deleting an existing 404 PURL via an HTTP DELETE.
	public void testDelete404Purl() {
		deletePurl("/testdomain/test404PURL");
	}
	
	// Test creating a new 410 PURL via an HTTP POST.
	public void testCreate410Purl() {
		createEasyPurl("/testdomain/test410PURL", "410", "testuser", null);
	}
	
	// Test resolving an existing 410 PURL via an HTTP GET.
	public void testResolve410Purl() {
		resolvePurl("/testdomain/test410PURL", "Gone");
	}

	// Test deleting an existing 410 PURL via an HTTP DELETE.
	public void testDelete410Purl() {
		deletePurl("/testdomain/test410PURL");
	}
	
	// Test creating a new Clone PURL via an HTTP POST.
	// TODO: Refactor this mess.
	public void testCreateClonePurl() {
		createPurl("/testdomain/testClonePURL", "clone", null, null, null, true);
	}
	
	// Test resolving an existing Clone PURL via an HTTP GET.
	public void testResolveClonePurl() {
		resolvePurl("/testdomain/testClonePURL", "http://example.com/test302PURL");
	}

	// Test deleting an existing Clone PURL via an HTTP DELETE.
	public void testDeleteClonePurl() {
		deletePurl("/testdomain/testClonePURL");
	}
	
	// Test creating a new Chain PURL via an HTTP POST.
	public void testCreateChainPurl() {
		createPurl("/testdomain/testChainPURL", "chain", "testuser", null, null, true);
	}
	
	// Test resolving an existing Chain PURL via an HTTP GET.
	public void testResolveChainPurl() {
		resolvePurl("/testdomain/testChainPURL", "http://localhost:8080/testdomain/test302PURL");
	}

	// Test deleting an existing Chain PURL via an HTTP DELETE.
	public void testDeleteChainPurl() {
		deletePurl("/testdomain/testChainPURL");
	}
	
	// Test creating a new Partial PURL via an HTTP POST.
	public void testCreatePartialPurl() {
		createEasyPurl("/testdomain/testPartialPURL", "partial", "testuser", "http://example.com/testPartialPURL");
	}
	
	// Test resolving an existing Partial PURL via an HTTP GET.
	public void testResolvePartialPurl() {
		resolvePurl("/testdomain/testPartialPURL/foobar", "http://example.com/testPartialPURL/foobar");
	}

	// Test deleting an existing Partial PURL via an HTTP DELETE.
	public void testDeletePartialPurl() {
		deletePurl("/testdomain/testPartialPURL");
	}
	
	
	
	/****************** Test Users **************************/

	// Test registering a new user via an HTTP POST.
	public void testRegisterUser() {
		registerUser("testuser", "Test User", "Zepheira", "test.user@example.com", "Testing!", "We are testing just now.", "Because unit tests are a necessary part of modern software development.");
	}
	
	// Test registering another user via an HTTP POST.
	public void testRegisterUser2() {
		registerUser("testuser2", "Another Test User", "Zepheira", "another.test.user@example.com", "passWord!", "", "");
	}

	// Test logging in as testuser.
	public void testLoginUser() {
		try {
			String url = "http://" + host + ":" + port + "/admin/login/login-submit.bsh";
						
			Map<String, String> formParameters = new HashMap<String,String> ();
			formParameters.put("id", "testuser");
			formParameters.put("passwd", "Testing!");
			formParameters.put("referrer", "/docs/index.html");
			
			String errMsg = "Cannot login testuser: ";
			String control = "";
			String test = client.login(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to login user: ", e);
		}
	}

	// Test modifying an existing user via an HTTP PUT.
	public void testModifyUser() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/testuser";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test User Modified");
			formParameters.put("affiliation", "Zepheira, LLC");
			formParameters.put("email", "tuser@example.com");
			formParameters.put("passwd", "TestingAgain");
			formParameters.put("hint", "We are still testing.");
			formParameters.put("justification", "Because we like unit tests.");
			
			String errMsg = "Cannot modify a User: ";
			String control = "Updated resource: testuser";
			String test = client.modifyUser(url, formParameters);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUser() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/testuser";

			String errMsg = "Cannot search user.";
			String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUserByName() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/?name=Test%20User%20Modified";

			String errMsg = "Cannot search user.";
			String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUserByAffiliation() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/?affiliation=Zepheira%2C%20LLC";

			String errMsg = "Cannot search user.";
			String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUserByEmail() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/?email=tuser%40example.com";

			String errMsg = "Cannot search user.";
			String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for users via an HTTP GET.
	public void testSearchUserByIdAndName() {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/testuser?name=Test%20User%20Modified";

			String errMsg = "Cannot search user.";
			String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
			String test = client.searchUser(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);						
			
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
	
	
	/****************** Test Groups **************************/
	
	// Test creating a new group via an HTTP POST.
	public void testCreateGroup() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A group used for unit tests.");
							
			String errMsg = "Cannot create a new group.";
			String control = "<group><id>testgroup</id><name>Test Group</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";
			String test = client.createGroup(url, formParameters);
			
			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test creating a new group with multiple members via an HTTP POST.
	public void testCreateGroupWithMultipleMaintainers() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup2";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 2");
			formParameters.put("maintainers", "testuser,testuser2");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A group used for unit tests.");
							
			String errMsg = "Cannot create a new group.";
			String control = "<group><id>testgroup2</id><name>Test Group 2</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";
			String test = client.createGroup(url, formParameters);
			
			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new group with multiple members via an HTTP POST.
	public void testCreateGroupWithAGroupAsMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup3";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 3");
			formParameters.put("maintainers", "testgroup,testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A group used for unit tests.");
							
			String errMsg = "Cannot create a new group.";
			String control = "<group><id>testgroup3</id><name>Test Group 3</name><maintainers><uid>testuser</uid><gid>testgroup</gid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";

			String test = client.createGroup(url, formParameters);
			
			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new group with multiple members via an HTTP POST.
	public void testCreateGroupWithMultipleMembers() {

		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup4";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 4");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser,testuser2");
			formParameters.put("comments", "A group used for unit tests.");

			String errMsg = "Cannot create a new group.";
			String control = "<group><id>testgroup4</id><name>Test Group 4</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid><uid>testuser2</uid></members><comments>A group used for unit tests.</comments></group>";
			String test = client.createGroup(url, formParameters);

			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test creating a new group with multiple members via an HTTP POST.
	public void testCreateGroupWithAGroupAsMember() {

		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup5";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 5");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testgroup");
			formParameters.put("comments", "A group used for unit tests.");

			String errMsg = "Cannot create a new group.";

			String control = "<group><id>testgroup5</id><name>Test Group 5</name><maintainers><uid>testuser</uid></maintainers><members><gid>testgroup</gid></members><comments>A group used for unit tests.</comments></group>";
			String test = client.createGroup(url, formParameters);

			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroup() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers>testuser</maintainers><members>testuser</members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupAddMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group Modified");
			formParameters.put("maintainers", "testuser,testuser2");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers>testuser</maintainers><members>testuser</members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupRemoveMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers>testuser</maintainers><members>testuser</members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupRemoveGroupAsMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup3";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 3 Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup3";
			//String control = "<group><id>testgroup3</id><name>Test Group Modified</name><maintainers><uid>testgroup</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupAddMember() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser,testuser2");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers>testuser</maintainers><members>testuser</members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupRemoveMember() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup";
			//String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}	
	
	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupRemoveGroupAsMember() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup5";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 5 Modified");
			formParameters.put("maintainers", "testuser");
			formParameters.put("members", "testuser");
			formParameters.put("comments", "A modified group used for unit tests.");
							
			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup5";
			//String control = "<group><id>testgroup5</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);
			
			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);
						
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}	
	
	// Test modifying an existing group via an HTTP PUT.
	// TODO: Replace/remove?
	public void testModifyGroupWithFile() {

			try {
				String url = "http://" + host + ":" + port + "/admin/group/testgroup";

				File file = new File(getTestDataFile("groupmodify.xml"));

				String errMsg = "Cannot modify a group: ";
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
			String url = "http://" + host + ":" + port + "/admin/group/testgroup";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.searchGroup(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroupByName() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/?name=Test%20Group%20Modified";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.searchGroup(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroupByMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/?maintainers=testuser";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.searchGroup(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroupByMember() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/?members=testuser";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.searchGroup(url);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for groups via an HTTP GET.
	public void testSearchGroupByIdAndName() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup?name=Test%20Group%20Modified";

			String errMsg = "Cannot search group.";
			String control = "<group><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
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
	
	
	/****************** Test Domains **************************/
	
	// Test creating a new domain via an HTTP POST.
	public void testCreateDomain() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain");
			formParameters.put("maintainers", "testuser");
			formParameters.put("writers", "testuser");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
			// TODO: DBG
			//assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new domain via an HTTP POST.
	public void testCreateDomainWithMultipleMaintainers() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain2";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain 2");
			formParameters.put("maintainers", "testuser,testuser2");
			formParameters.put("writers", "testuser");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>/testdomain2</id><name>Test Domain 2</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
			XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
			// TODO: DBG
			//assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new domain via an HTTP POST.
	public void testCreateDomainWithAGroupAsMaintainer() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain3";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain 3");
			formParameters.put("maintainers", "testgroup, testuser");
			formParameters.put("writers", "testuser");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>/testdomain3</id><name>Test Domain 3</name><maintainers><uid>testuser</uid><gid>testgroup</gid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
	        XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
			// TODO: DBG
			//assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new domain via an HTTP POST.
	public void testCreateDomainWithMultipleWriters() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain4";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain 4");
			formParameters.put("maintainers", "testuser");
			formParameters.put("writers", "testuser,testuser2");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>/testdomain4</id><name>Test Domain 4</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid><uid>testuser2</uid></writers><public>false</public></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
	        XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
			// TODO: DBG
			//assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test creating a new domain via an HTTP POST.
	public void testCreateDomainWithAGroupAsWriter() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain5";
			
			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Domain 5");
			formParameters.put("maintainers", "testuser");
			formParameters.put("writers", "testgroup");
			formParameters.put("public", "false");
			
			String errMsg = "Cannot create a new domain.";
			String control = "<domain><id>/testdomain5</id><name>Test Domain 5</name><maintainers><uid>testuser</uid></maintainers><writers><gid>testgroup</gid></writers><public>false</public></domain>";
			String test = client.createDomain(url, formParameters);
			
			// XML response, so use assertXMLEqual.
	        XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + " : " + test, control, test);
			// TODO: DBG
			//assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}			
	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomain() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainAddMaintainer() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain Modified");
				formParameters.put("maintainers", "testuser,testuser2");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainRemoveMaintainer() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainRemoveGroupAsMaintainer() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain3";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain 3 Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain3";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainAddWriter() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser,testuser2");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainRemoveWriter() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test modifying an existing domain via an HTTP PUT.
	public void testModifyDomainRemoveGroupAsWriter() {

			try {
				String url = "http://" + host + ":" + port + "/admin/domain/testdomain5";

				Map<String, String> formParameters = new HashMap<String, String>();
				formParameters.put("name", "Test Domain 5 Modified");
				formParameters.put("maintainers", "testuser");
				formParameters.put("writers", "testuser");
				formParameters.put("public", "true");

				String errMsg = "Cannot modify a Domain: ";
				String control = "Updated resource: /testdomain5";
				String test = client.modifyDomain(url, formParameters);

				// Textual response, so use assertEquals.
				assertEquals(errMsg + test, control, test);

			} catch (Exception e) {
				reportException("Failed to resolve URL: ", e);
			}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomain() {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.searchDomain(url);
			
			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);			
			XMLAssert.assertXMLEqual(errMsg, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomainByName() {

		try {
			String url = "http://" + host + ":" + port + "/admin/domain/?name=Test%20Domain%20Modified";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.searchDomain(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomainByMaintainer() {

		try {
			String url = "http://" + host + ":" + port + "/admin/domain/?maintainers=testuser";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.searchDomain(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomainByWriter() {

		try {
			String url = "http://" + host + ":" + port + "/admin/domain/?writers=testuser";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.searchDomain(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test searching for domains via an HTTP GET.
	public void testSearchDomainByIdAndName() {

		try {
			String url = "http://" + host + ":" + port + "/admin/domain/testdomain?name=Test%20Domain%20Modified";

			String errMsg = "Cannot search domain.";
			String control = "<domain><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers><public>false</public></domain>";
			String test = client.searchDomain(url);

			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);

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
	
	
	/****************** Utility Methods **************************/
		
	// Convenience method for creating PURLs of types 301, 302, 307, 404 and 410.
	public void createEasyPurl(String path, String type, String maintainers, String target) {
		createPurl(path, type, maintainers, target, null, false);
	}
	
	// Convenience method for creating PURLs which are expected to succeed.
	public void createPurl(String path, String type, String maintainers, String target, String seealso, boolean useBasepurl) {
		createPurl( path,  type,  maintainers,  target,  seealso,  useBasepurl, true);
	}
	
	/** Create a new PURL via an HTTP POST.
	  * @param path A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
	  * @param type The type of PURL (one of "301", "302", "303", "307", "404", "410", "clone", "chain", "partial").
	  * @param maintainers A comma-separated list of user or group names that are allowed to maintain the named PURL.
	  * @param target A URL to redirect to; used for PURLs of type 301, 302, 307 and partial.
	  * @param seealso A URL for See Also requests; used for PURLs of type 303.
	  * @param useBasepurl Whether or not to include a default basePURL for testing of PURLs of types clone or chain.
	  * @param expectSuccess Whether the test should expect to succeed.  If false, it will expect to fail.
	*/
	public void createPurl(String path, String type, String maintainers, String target, String seealso, boolean useBasepurl, boolean expectSuccess) {
		
		try {
			String url = "http://" + host + ":" + port + "/admin/purl" + path;
			String control = "<purl><id>" + path + "</id>";
			if ( type.equals("clone") ) {
				// For chaining and cloning PURLs.
				// NB: Presumes use of the hard-coded test data!
				control += "<type>302</type>";                                                      
			} else {
				// For most types of PURLS.
				control += "<type>" + type + "</type>";
			}
						
			Map<String, String> formParameters = new HashMap<String,String> ();
			formParameters.put("type", type);
			
			if ( maintainers != null ) {
				formParameters.put("maintainers", maintainers);
				control += "<maintainers><uid>" + maintainers + "</uid></maintainers>";
			}
			if ( target != null ) {
				formParameters.put("target", target);
				control += "<target><url>" + target + "</url></target>";
			}
			if ( seealso != null ) {
				formParameters.put("seealso", seealso);
				control += "<seealso><url>" + seealso + "</url></seealso>";
			}
			if ( type == "clone" ) {
				// For cloning.
				// Note use of hardcoded basePURL, which must be created first.
				formParameters.put("basepurl", "/testdomain/test302PURL");
				control += "<target><url>http://example.com/test302PURL</url></target><maintainers><uid>testuser</uid></maintainers>";
			} else if ( type == "chain" ) {
				// For chaining.
				// Note use of hardcoded basePURL, which must be created first.
				formParameters.put("basepurl", "/testdomain/test302PURL");
				control += "<target><url>/testdomain/test302PURL</url></target>";
			}
			control += "</purl>";

			String errMsg = "Cannot create a new " + type + " PURL: ";
			
			String test = client.createPurl(url, formParameters);
			

			// Convert the test and control values to lower case for clean comparison.
			String testLC = test.toLowerCase();
			String controlLC = control.toLowerCase();
			
			if (expectSuccess) {
				// This test expects to succeed.
				try {
					// XML response, so use assertXMLEqual.
					XMLAssert.assertXMLEqual(errMsg + test, controlLC, testLC);
				} catch (Throwable e){
					// XMLUnit can throw an error if the test or control are not XML.
					// Unfortunately, the error is very poorly described
					// ("Content is not allowed in prolog."), so we change
					// it here to avoid confusion.
					throw new Exception ("Bad input.  XMLUnit expected XML, but received plain text.");
				}
			} else {
				// This test expects to fail.
				// Override the control text:
				control = "PURL: " + path + " already exists.";
				controlLC = control.toLowerCase();
				// Textual response, so use assertEquals().
				assertEquals(errMsg, controlLC, testLC);
			}
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	/** Resolve a PURL via HTTP GET.
	  * @param path A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
	  * @param control An expected response string from the resolution.  Generally, this will be a URL from a PURL's Location header.
	*/
	public void resolvePurl(String path, String control) {
		
		try {
			String url = "http://" + host + ":" + port + path;

			String errMsg = "Cannot resolve PURL: ";
			String test = client.resolvePurl(url);
			
			// Convert the test and control values to lower case for clean comparison.
			if ( test != null ) {
				test = test.toLowerCase();
			}
			if ( control != null ) {
				control = control.toLowerCase();
			}
			
			// Textual response, so use assertEquals().
			assertEquals(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
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
	
	/** Register a new user via an HTTP POST.
	  * 
	  * NB: Assumes that immediate user registration is available in the PURL server.
	  *     To turn on immediate user registration, unset <allowUserAutoCreation/>
	  *     in src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
	  *
	  * @param uid A user id (e.g. "dwood").
	  * @param name The name of the user (e.g. "David Wood").
	  * @param affiliation The user's affiliation (such as a company name).
 	  * @param email The user's email address
	  * @param passwd The user's new password (to create the account; a password is not required to authenticate yet).
	  * @param hint A hint that they user can later user to remember their password.
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
			if ( userAutoCreationOn != true && userAutoCreationOn != false ) {
				// Set userAutoCreationOn the first time it can be determined.
				if ( Pattern.matches("status=\"0\"", test) ) {
					userAutoCreationOn = false;
				} else if ( Pattern.matches("status=\"1\"", test) ) {
						userAutoCreationOn = true;
				} else {
					// The status attribute was not found in the server's response.
					// This is an error condition;
					fail("Test registerUser() failed to understand the message from the server.");
				}
			}
			
			if ( userAutoCreationOn ) {
				userAutoCreationFlag = 0;
			}
			String control = "<user admin=\"false\" status=\"" + userAutoCreationFlag + "\"><id>" + uid + "</id><name>" + name + "</name><affiliation>" + affiliation + "</affiliation><email>" + email + "</email></user>";
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	/** Delete a user via an HTTP DELETE.
	  *
	  * @param uid A user id (e.g. "dwood").
	*/
	public void deleteUser(String uid) {
	    deleteUser(uid, false, true);
	}
	
	public void deleteUser(String uid, boolean loginFirst, boolean expectSuccess) {

	    if(loginFirst) {
	        // For now we assume we want to login as testuser2

	        String url = "http://" + host + ":" + port + "/admin/login/login-submit.bsh";

	        Map<String, String> formParameters = new HashMap<String,String> ();
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

	    if(expectSuccess) {
	        errMsg = "Cannot delete User.";         
	        control = "Deleted resource: " + uid;           
	    } else {
	        errMsg = "Could (but shouldn't) delete User.";         
	        control = "Not allowed to delete: " + uid;	        
	    }

	    // Textual response, so use assertEquals().
	    assertEquals(errMsg, control, test);
	}
	
	/** Create a batch of PURLs via an HTTP POST.
	  *
	  * @param filename The name of a file in the "testdata" directory that holds input data.
	  * @param numCreated The number of PURLs to be created in this operation.
	*/
	public void createPurls(String filename, int numCreated) {

		try {
			String url = "http://" + host + ":" + port + "/admin/purls/";
			File file = new File(getTestDataFile(filename));

			String errMsg = "Cannot create a batch of PURLs.";
			String control = "<purl-batch-success numCreated=\"" + numCreated + "\"/>";
			String test = client.createPurls(url, file);
			
			// XML response, so use assertXMLEqual.
			XMLAssert.assertXMLEqual(errMsg + test, control, test);
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}
	
	private String getTestDataFile(String filename) {
        String separator = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");
        
        StringBuffer sb = new StringBuffer(System.getProperty("user.dir"));
        sb.append(separator);
        
        if(!userDir.endsWith("test")) {
            sb.append("test");
            sb.append(separator);               
        }
        
        sb.append("testdata");
        sb.append(separator);
        sb.append(filename);
        return sb.toString();
	}

	/** Read in the contents of a file and return them.
	  *
	  * @param filename The name of a file to read.
	  * @return The contents of the file.
	  */
    public static String readFile(String filename) throws FileNotFoundException, IOException {
	
		File file = new File(filename);
		String content = "";
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		fis = new FileInputStream(file);
		bis = new BufferedInputStream(fis);
		dis = new DataInputStream(bis);

		while (dis.available() != 0) {
			content += dis.readLine();
		}

		fis.close();
		bis.close();
		dis.close();
		
		return content;
    }

	// Handle any exceptions encountered above.  For JUnit tests, it
	// is enough to call JUnit's fail() method.
	private void reportException(String message, Exception e) {
		fail( message + e.getMessage() + " : " + e.getCause() );
	}

}
