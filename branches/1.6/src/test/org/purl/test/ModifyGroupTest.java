package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 *
 */
public class ModifyGroupTest extends AbstractIntegrationTest {


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
			formParameters.put("maintainers", "testuser2");
			formParameters.put("members", "testuser2");
			formParameters.put("comments", "A modified group used for unit tests.");

			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup5";
			//String control = "<group><id>testgroup5</id><name>Test Group Modified</name><maintainers><uid>testuser2</uid></maintainers><members><uid>testuser2</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

    	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupAddGroupAsMaintainer() {

		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup5";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 5 Modified");
			formParameters.put("maintainers", "testuser2,testgroup4");
			formParameters.put("members", "testuser2");
			formParameters.put("comments", "A modified group used for unit tests.");

			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup5";
			//String control = "<group><id>testgroup5</id><name>Test Group Modified</name><maintainers><uid>testuser2</uid></maintainers><members><uid>testuser2</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

        	// Test modifying an existing group via an HTTP PUT.
	public void testModifyGroupAddGroupAsMember() {

		try {
			String url = "http://" + host + ":" + port + "/admin/group/testgroup5";

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", "Test Group 5 Modified");
			formParameters.put("maintainers", "testuser2");
			formParameters.put("members", "testuser2, testgroup4");
			formParameters.put("comments", "A modified group used for unit tests.");

			String errMsg = "Cannot modify a group.";
			String control = "Updated resource: testgroup5";
			//String control = "<group><id>testgroup5</id><name>Test Group Modified</name><maintainers><uid>testuser2</uid></maintainers><members><uid>testuser2</uid></members><comments>A modified group used for unit tests.</comments></group>";
			String test = client.modifyGroup(url, formParameters);

			// Textual response, so use assertEquals.
			assertEquals(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

	// Test modifying an existing group via an HTTP PUT.
	// TODO: Replace/remove?
//	public void testModifyGroupWithFile() {
//
//			try {
//				String url = "http://" + host + ":" + port + "/admin/group/testgroup";
//
//				File file = new File(getTestDataFile("groupmodify.xml"));
//
//				String errMsg = "Cannot modify a group: ";
//				String control = "Updated resource: testgroup";
//				String test = client.modifyGroup(url, file);
//
//				// Textual response, so use assertEquals.
//				assertEquals(errMsg + test, control, test);
//
//			} catch (Exception e) {
//				reportException("Failed to resolve URL: ", e);
//			}
//	}

}
