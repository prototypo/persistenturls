package org.purl.test;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class CreateGroupTest extends AbstractIntegrationTest {

    /**
     * *************** Test Groups *************************
     */

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
            String control = "<group status=\"1\"><id>testgroup</id><name>Test Group</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";
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
            String control = "<group status=\"1\"><id>testgroup2</id><name>Test Group 2</name><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";
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
            String control = "<group status=\"1\"><id>testgroup3</id><name>Test Group 3</name><maintainers><uid>testuser</uid><gid>testgroup</gid></maintainers><members><uid>testuser</uid></members><comments>A group used for unit tests.</comments></group>";

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
            String control = "<group status=\"1\"><id>testgroup4</id><name>Test Group 4</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid><uid>testuser2</uid></members><comments>A group used for unit tests.</comments></group>";
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

            String control = "<group status=\"1\"><id>testgroup5</id><name>Test Group 5</name><maintainers><uid>testuser</uid></maintainers><members><gid>testgroup</gid></members><comments>A group used for unit tests.</comments></group>";
            String test = client.createGroup(url, formParameters);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + ".  Response from server: " + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    public void testCreateHierachicalGroups() throws Exception {

        assertLogoutUser();
        assertLoginUser("admin", "password");
        createGroup("hierarchgroup1", "testuser", "testuser", "");
        createGroup("hierarchgroup2", "hierarchgroup1", "hierarchgroup1", "");
        createGroup("hierarchgroup3", "hierarchgroup2", "hierarchgroup2", "");
        assertLogoutUser();
        assertLoginUser("testuser", ":'[]{}Testing~!@#$%^&*()_+|\\,./';<>?\"");
    }







}
