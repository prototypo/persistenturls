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
    protected void setUp() {
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

        } catch (Exception e) {
            reportException("Failed to login user: ", e);
        }
    }

	// Log out the user associated with the currently set cookie.
    public void assertLogoutUser() {
        try {
            String url = "http://" + host + ":" + port + "/admin/logout";

            String errMsg = "Cannot logout user: ";
            String control = "";
            String test = client.logout(url);

            // Textual response, so use assertEquals.
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to logout user: ", e);
        }
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

}
