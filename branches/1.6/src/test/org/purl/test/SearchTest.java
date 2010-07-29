package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.Diff;

/**
 *
 */
public class SearchTest extends AbstractIntegrationTest {

    private static boolean slept = false;

    public static void setUpClass() {

    }


    protected void setUp() {
		try {
        	super.setUp();
		} catch (Exception e) {
			System.out.println("ERROR: Unexpected Exception in setup: " + e);
			System.out.println("Exiting...");
			System.exit(1);
		}
        if (!slept) {
            // TODO This is EVIL.  Move integration tests out of junit
            // Sleep for a while to ensure the SOLR service indexes the information before searching.
            try {
                Thread.sleep(80000); // The index updates every 60 seconds
                slept = true;
            } catch (InterruptedException ie) {
                System.out.println("WARNING: InterruptedException while trying to sleep.");
            }
        }

    }


    // Test searching for users via an HTTP GET.
    public void testSearchUser() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/testuser";

            String errMsg = "Cannot search user.";
            String control = "<user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user>";
            String test = client.searchUser(url);

            reportResult("testSearchUser", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

        // Test searching for users via an HTTP GET.
    public void testSearchUserWildcard() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/?userid=t*r";

            String test = client.searchUser(url);

            XMLAssert.assertXpathExists("/results/user[id='testuser']",test);
            XMLAssert.assertXpathNotExists("/results/user[id='testuser2']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for users via an HTTP GET.
    public void testSearchUserByName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/?fullname=Test%20User%20Modified";

            String test = client.searchUser(url);

            XMLAssert.assertXpathExists("/results/user[id='testuser']",test);
            XMLAssert.assertXpathNotExists("/results/user[id='testuser2']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for users via an HTTP GET.
    public void testSearchUserByAffiliation() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/?affiliation=Zepheira%2C%20LLC";

            String errMsg = "Cannot search user.";
            String control = "<results><user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user><user admin=\"false\" status=\"1\"><id>testuser2</id><name>Another Test User</name><affiliation>Zepheira</affiliation><email>another.test.user@example.com</email></user></results>";
            String test = client.searchUser(url);

            reportResult("testSearchUserByAffiliation", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
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
            String control = "<results><user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user></results>";
            String test = client.searchUser(url);

            XMLAssert.assertXpathExists("/results/user[id='testuser']",test);
            XMLAssert.assertXpathNotExists("/results/user[id='testuser2']",test);

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

            reportResult("testSearchUserByIdAndName", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroup() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/testgroup";

            String errMsg = "Cannot search group.";
            String control = "<group status=\"1\"><id>testgroup</id><name>Test Group Modified</name><maintainers><uid>testuser</uid></maintainers><members><uid>testuser</uid></members><comments>A modified group used for unit tests.</comments></group>";
            String test = client.searchGroup(url);

            reportResult("testSearchGroup", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroupByName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/?name=Test%20Group%205%20Modified";

            String errMsg = "Cannot search group.";

            String control = "<results><group status=\"1\"><id>testgroup5</id><name>Test Group 5 Modified</name><comments>A modified group used for unit tests.</comments><maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><members><uid>testuser2</uid></members></group></results>";

            String test = client.searchGroup(url);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup2']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup3']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup4']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup5']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroupByMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/?maintainers=testuser2";

            String errMsg = "Cannot search group.";
            String control = "<results><group status=\"1\"><id>testgroup2</id><name>Test Group 2</name><comments>A group used for unit tests.</comments><maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><members><uid>testuser</uid></members></group><group status=\"1\"><id>testgroup5</id><name>Test Group 5 Modified</name><comments>A modified group used for unit tests.</comments><maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><members><uid>testuser2</uid></members></group></results>";

            String test = client.searchGroup(url);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup2']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup3']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup4']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup5']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroupByMember() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/?members=testuser2";

            String errMsg = "Cannot search group.";
            String control = "<results><group status=\"1\"><id>testgroup5</id><name>Test Group 5 Modified</name><comments>A modified group used for unit tests.</comments>" +
                    "<maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><members><uid>testuser2</uid></members></group><group status=\"1\">" +
                    "<id>testgroup4</id><name>Test Group 4</name><comments>A group used for unit tests.</comments><maintainers><uid>testuser</uid></maintainers>" +
                    "<members><uid>testuser</uid><uid>testuser2</uid></members></group></results>";

            String test = client.searchGroup(url);

            XMLAssert.assertXpathNotExists("/results/group[id='testgroup']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup2']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup3']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup4']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup5']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroupByIdAndName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/?id=testgroup&name=Test%20Group%205%20Modified";

            String test = client.searchGroup(url);

            XMLAssert.assertXpathExists("/results/group[id='testgroup']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup2']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup3']",test);
            XMLAssert.assertXpathNotExists("/results/group[id='testgroup4']",test);
            XMLAssert.assertXpathExists("/results/group[id='testgroup5']",test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test searching for domains via an HTTP GET.
    public void testSearchDomain() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            String test = client.searchDomain(url);
            XMLAssert.assertXpathExists("/domain[id='/testdomain']", test);
            XMLAssert.assertXpathNotExists("/domain[id='/testdomain2']", test);
            XMLAssert.assertXpathNotExists("/domain[id='/testdomain3']", test);
            XMLAssert.assertXpathNotExists("/domain[id='/testdomain4']", test);
            XMLAssert.assertXpathNotExists("/domain[id='/testdomain5']", test);
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?name=Test%20Domain%203%20Modified";

            String test = client.searchDomain(url);

            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain2']", test);
            XMLAssert.assertXpathExists("/results/domain[id='/testdomain3']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain4']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain5']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByMaintainer() throws Exception {
        String url = "http://" + host + ":" + port + "/admin/domain/?maintainers=testuser2";

        String test = client.searchDomain(url);

        XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain']", test);
        XMLAssert.assertXpathExists("/results/domain[id='/testdomain2']", test);
        XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain3']", test);
        XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain4']", test);
        XMLAssert.assertXpathExists("/results/domain[id='/testdomain5']", test);

    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?writers=testuser2";

            String test = client.searchDomain(url);

            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain2']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain3']", test);
            XMLAssert.assertXpathExists("/results/domain[id='/testdomain4']", test);
            XMLAssert.assertXpathExists("/results/domain[id='/testdomain5']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByIdAndName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?id=/testdomain5&name=Test%20Domain%205%20Modified";

            String test = client.searchDomain(url);

            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain2']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain3']", test);
            XMLAssert.assertXpathNotExists("/results/domain[id='/testdomain4']", test);
            XMLAssert.assertXpathExists("/results/domain[id='/testdomain5']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurl() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?path=/testdomain/testPURL";
            String test = client.searchPurl(url);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test301PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test302PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test303PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test307PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test404PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test410PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testClonePURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testChainPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testPartialPURL']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

        // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlWildcard() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?path=/test*/te%3FtPURL";
            String test = client.searchPurl(url);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test301PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test302PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test303PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test307PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test404PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test410PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testClonePURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testChainPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testPartialPURL']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

        // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlWildcardFail() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?path=/test%3F/testPURL";
            String test = client.searchPurl(url);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test301PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test302PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test303PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test307PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test404PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test410PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testClonePURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testChainPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testPartialPURL']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlByTarget() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?target=http://bbc.co.uk/";
            String test = client.searchPurl(url);

            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test301PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test302PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test303PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test307PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test404PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/test410PURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testClonePURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testChainPURL']", test);
            XMLAssert.assertXpathNotExists("/results/purl[id='/testdomain/testPartialPURL']", test);             

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlByMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?maintainers=testuser";

            String test = client.searchPurl(url);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testPURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test301PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test302PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test303PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test307PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test404PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/test410PURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testClonePURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testChainPURL']", test);
            XMLAssert.assertXpathExists("/results/purl[id='/testdomain/testPartialPURL']", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        return suite;
    }

}
