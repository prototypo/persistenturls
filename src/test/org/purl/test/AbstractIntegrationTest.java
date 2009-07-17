package org.purl.test;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AbstractIntegrationTest extends TestCase {

    protected PurlTestClient client = null;
    // Set the host and port for all subsequent URLs.
    protected String host = "localhost";
    protected String port = "8080";

    // Handle any exceptions encountered above.  For JUnit tests, it
    // is enough to call JUnit's fail() method.
    protected void reportException(String message, Exception e) {
        fail(message + e.getMessage() + " : " + e.getCause());
    }

    protected void reportResult(String testName, String testResult) {
        //System.err.println("\n\nTEST RESULT FOR " + testName + ":\n" + testResult);

    }

    // Create an instance of the PURL test client for all methods to use.
    protected void setUp() throws Exception {
        if (client == null) {
            client = new PurlTestClient();
        }
    }

    // Log in a registered user.
    public void assertLoginUser(String user, String password) {
        try {
            String url = "http://" + host + ":" + port + "/admin/login/login-submit.bsh";

            Map<String, String> formParameters = new HashMap<String, String>();
            formParameters.put("id", user);
            formParameters.put("passwd", password);
            formParameters.put("referrer", "/docs/index.html");

            String errMsg = "Cannot login " + user + ": ";
            String control = "";
            String test = client.login(url, formParameters);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);
            assertLoggedIn(user);

        } catch (Exception e) {
            reportException("Failed to login user: ", e);
        }
    }

    public void assertFailLoginUser(String user, String password) {
        try {

            String errMsg = "Cannot login " + user + ": ";
            String control = "";
            String test = loginUser(user, password);

            assertLoggedOut();

        } catch (Exception e) {
            reportException("Failed to login user: ", e);
        }
    }

    public void assertLogoutUser() {
        try {
            String url = "http://" + host + ":" + port + "/admin/logout";

            String errMsg = "Cannot logout user: ";
            String control = "";
            String test = client.logout(url);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);
            assertLoggedOut();

        } catch (Exception e) {
            reportException("Failed to logout user: ", e);
        }
    }

    protected String logoutUser() throws Exception {
        String url = "http://" + host + ":" + port + "/admin/logout";

        return client.logout(url);

    }

    protected void assertLoggedIn(String user) {
        try {
            String url = "http://" + host + ":" + port + "/admin/loginstatus";

            String status = client.loginstatus(url);

            
            XMLAssert.assertXpathExists("/login[uid='" + user + "']", status);

        } catch (Exception e) {
            reportException("Failed to fetch login status: ", e);
        }
    }

    protected void assertLoggedOut() {
        try {
            String url = "http://" + host + ":" + port + "/admin/loginstatus";

            String status = client.loginstatus(url);

            XMLAssert.assertXpathExists("/login[status='logged out']", status);

        } catch (Exception e) {
            reportException("Failed to fetch login status: ", e);
        }
    }

    protected boolean isLoggedIn() throws Exception {
        String url = "http://" + host + ":" + port + "/admin/loginstatus";

        String status = client.loginstatus(url);
        if (status.contains("logged in"))
            return true;
        return false;
    }

    protected String loginUser(String user, String password) throws Exception {
        String url = "http://" + host + ":" + port + "/admin/login/login-submit.bsh";

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("id", user);
        formParameters.put("passwd", password);
        formParameters.put("referrer", "/docs/index.html");


        String result = client.login(url, formParameters);
        return result;
    }


    protected String getTestDataFile(String filename) {
        String separator = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");

        StringBuffer sb = new StringBuffer(System.getProperty("user.dir"));
        sb.append(separator);

        if (!userDir.endsWith("test")) {
            sb.append("test");
            sb.append(separator);
        }

        sb.append("testdata");
        sb.append(separator);
        sb.append(filename);
        return sb.toString();
    }

    /**
     * Read in the contents of a file and return them.
     *
     * @param filename The name of a file to read.
     * @return The contents of the file.
     */
    protected static String readFile(String filename) throws FileNotFoundException, IOException {

        File file = new File(filename);
        String content = "";
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String in;
        while ((in = br.readLine()) != null) {
            content += in;
        }

        fis.close();
        bis.close();
        br.close();

        return content;
    }

    /**
     * Delete a PURL via HTTP DELETE.
     *
     * @param path          A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
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


    public void assertDomainCreated(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        XMLAssert.assertXpathExists("/domain[@status='1']", test);
        XMLAssert.assertXpathExists("/domain[id='" + path + "']", test);
    }

    public void assertDomainNotCreatedAlreadyExists(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        assertTrue("Expected " + path + " to already exist.  Received message: " + test, test.endsWith("cannot be created because it already exists."));
    }

    public void assertDomainNotCreatedNoRoot(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {
        String test = createDomain(path, name, maintainers, writers, isPublic);
        assertTrue("Expected " + path + " to require root domain.  Received message: " + test, test.contains("cannot be created because the root domain"));
    }


    public String createDomain(String path, String name, String maintainers, String writers, boolean isPublic) throws Exception {

        String url = "http://" + host + ":" + port + "/admin/domain" + path;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", name);
        formParameters.put("maintainers", maintainers);
        formParameters.put("writers", writers);
        formParameters.put("public", Boolean.toString(isPublic));

        return client.createDomain(url, formParameters);

    }


    public String modifyDomain(String domain, String name, String maintainers, String writers, String isPublic) throws Exception {
        String url = "http://" + host + ":" + port + "/admin/domain" + domain;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", name);
        formParameters.put("maintainers", maintainers);
        formParameters.put("writers", writers);
        formParameters.put("public", isPublic);

        return client.modifyDomain(url, formParameters);
    }

    protected String createGroup(String group, String maintainers, String members, String comments) throws Exception {

        String url = "http://" + host + ":" + port + "/admin/group/" + group;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", group);
        formParameters.put("maintainers", maintainers);
        formParameters.put("members", members);
        formParameters.put("comments", comments);
        return client.createGroup(url, formParameters);

    }

    protected void assertPurlCreated(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = createPurl(path, formParameters);
            XMLAssert.assertXpathExists("/purl[@status='1']", test);
            XMLAssert.assertXpathExists("/purl[id='" + path + "']", test);
            if (formParameters.get("target") != null) {
                XMLAssert.assertXpathExists("/purl[target='" + formParameters.get("target") + "']", test);
            }
            resolvePurlMetdata(path);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    protected void assertPurlCreated(String path, String target, String type) {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", type);
        formParameters.put("target", target);
        formParameters.put("maintainers", "testuser");

        assertPurlCreated(path, formParameters);
    }

    protected void assertPurlNotCreated(String path, Map<String, String> formParameters, String control) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = createPurl(path, formParameters);

            assertEquals(errMsg, control, test);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    protected void assertPurlNotCreated(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = createPurl(path, formParameters);

            assertFalse(errMsg, test.contains("<purl"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    protected void assertPurlNotCreatedAlreadyExists(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = createPurl(path, formParameters);

            assertTrue(test.contains("already exists."));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    protected void assertPurlModified(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = modifyPurl(path, formParameters);
            assertEquals(errMsg, "Updated resource: " + path, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    protected void assertPurlNotModified(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = modifyPurl(path, formParameters);


            assertFalse(errMsg, ("Updated resource: " + path).equals(test));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    protected void assertPurlNotModifiedNoSuchResource(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = modifyPurl(path, formParameters);


            assertTrue(errMsg, test.contains("No such resource"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    protected void assertPurlNotModifiedNotAllowed(String path, Map<String, String> formParameters) {
        try {


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = modifyPurl(path, formParameters);


            assertTrue(errMsg, test.contains("Not allowed to update"));
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    protected String createPurl(String path, Map<String, String> formParameters) throws Exception {
        String url = "http://" + host + ":" + port + "/admin/purl" + path;
        return client.createPurl(url, formParameters);
    }


    protected String modifyPurl(String path, Map<String, String> formParameters) throws Exception {
        String url = "http://" + host + ":" + port + "/admin/purl" + path;
        return client.modifyPurl(url, formParameters);
    }


    protected String modifyGroup(String group, String name, String maintainers, String members, String comments) throws Exception {
        String url = "http://" + host + ":" + port + "/admin/group/" + group;

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("name", name);
        formParameters.put("maintainers", maintainers);
        formParameters.put("members", members);
        formParameters.put("comments", comments);

        return client.modifyGroup(url, formParameters);
    }

    public void resolvePurlMetdata(String path) throws Exception {
       String url1 = "http://" + host + ":" + port + "/purl" + path;
       String url2 = "http://" + host + ":" + port + "/admin/purl" + path;
       assertEquals(client.resolvePurl(url1), client.resolvePurl(url2));
   }
}
