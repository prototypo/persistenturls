package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/**
 *
 */
public class SearchTest extends AbstractIntegrationTest {

    private static boolean slept = false;

    public static void setUpClass() {

    }


    protected void setUp() {
        super.setUp();
        if (!slept) {
            // TODO This is EVIL.  Move integration tests out of junit
            // Sleep for a while to ensure the SOLR service indexes the information before searching.
            try {
                Thread.sleep(70000); // The index updates every 60 seconds
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
    public void testSearchUserByName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/user/?fullname=Test%20User%20Modified";

            String errMsg = "Cannot search user.";
            String control = "<results><user admin=\"false\" status=\"1\"><id>testuser</id><name>Test User Modified</name><affiliation>Zepheira, LLC</affiliation><email>tuser@example.com</email></user></results>";
            String test = client.searchUser(url);

            reportResult("testSearchUserByName", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
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

            reportResult("testSearchUserByEmail", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
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

            reportResult("testSearchGroupByName", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

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

            reportResult("testSearchGroupByMaintainer", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

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

            reportResult("testSearchGroupByMember", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for groups via an HTTP GET.
    public void testSearchGroupByIdAndName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/group/?id=testgroup&name=Test%20Group%205%20Modified";

            String errMsg = "Cannot search group.";
            String control = "<results><group status=\"1\"><id>testgroup5</id><name>Test Group 5 Modified</name><comments>A modified group used for unit tests.</comments>" +
                    "<maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><members><uid>testuser2</uid></members></group></results>";
            String test = client.searchGroup(url);

            reportResult("testSearchGroupByIdAndName", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test searching for domains via an HTTP GET.
    public void testSearchDomain() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/testdomain";

            String errMsg = "Cannot search domain.";
            String control = "<domain status=\"1\"><id>/testdomain</id><name>Test Domain Modified</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid></writers><public>true</public></domain>";
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
            String url = "http://" + host + ":" + port + "/admin/domain/?name=Test%20Domain%203%20Modified";

            String errMsg = "Cannot search domain.";
            String control = "<results><domain status=\"1\"><id>/testdomain3</id><name>Test Domain 3 Modified</name><maintainers><uid>testuser</uid></maintainers><writers><uid>testuser</uid></writers><public>true</public></domain></results>";
            String test = client.searchDomain(url);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?maintainers=testuser2";

            String errMsg = "Cannot search domain.";
            String control = "<results><domain status=\"1\"><id>/testdomain2</id><name>Test Domain 2</name><public>false</public>" +
                    "<maintainers><uid>testuser</uid><uid>testuser2</uid></maintainers><writers><uid>testuser</uid></writers></domain><domain status=\"1\">" +
                    "<id>/testdomain5</id><name>Test Domain 5 Modified</name><public>true</public><maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers>" +
                    "<writers><uid>testuser2</uid></writers></domain></results>";

            String test = client.searchDomain(url);

            reportResult("testSearchDomainByMaintainer", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByWriter() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?writers=testuser2";

            String errMsg = "Cannot search domain.";
            String control = "<results><domain status=\"1\"><id>/testdomain5</id><name>Test Domain 5 Modified</name><public>true</public>" +
                    "<maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><writers><uid>testuser2</uid></writers></domain><domain status=\"1\">" +
                    "<id>/testdomain4</id><name>Test Domain 4</name><public>false</public><maintainers><uid>testuser</uid></maintainers>" +
                    "<writers><uid>testuser</uid><uid>testuser2</uid></writers></domain></results>";

            String test = client.searchDomain(url);

            reportResult("testSearchDomainByWriter", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for domains via an HTTP GET.
    public void testSearchDomainByIdAndName() {

        try {
            String url = "http://" + host + ":" + port + "/admin/domain/?id=testdomain5&name=Test%20Domain%205%20Modified";

            String errMsg = "Cannot search domain.";
            String control = "<results><domain status=\"1\"><id>/testdomain5</id><name>Test Domain 5 Modified</name><public>true</public>" +
                    "<maintainers><uid>testuser2</uid><uid>testuser</uid></maintainers><writers><uid>testuser2</uid></writers></domain></results>";

            String test = client.searchDomain(url);

            reportResult("testSearchDomainByIdAndName", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }


    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurl() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?path=/testdomain/testPURL";

            String errMsg = "Cannot search PURL.  Returned message was: ";
            String control = "<results><purl status=\"1\"><id>/testdomain/testPURL</id><type>302</type><maintainers><uid>testuser</uid></maintainers><target><url>http://bbc.co.uk/</url></target></purl></results>";
            String test = client.searchPurl(url);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlByTarget() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?target=http://bbc.co.uk/";

            String errMsg = "Cannot search PURL.  Returned message was: ";
            String control = "<results><purl status=\"1\"><id>/testdomain/testPURL</id><type>302</type><maintainers><uid>testuser</uid></maintainers><target><url>http://bbc.co.uk/</url></target></purl></results>";
            String test = client.searchPurl(url);

            reportResult("testSearchPurlByTarget", test);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    // Test searching for PURLs via an HTTP GET.
    public void testSearchPurlByMaintainer() {

        try {
            String url = "http://" + host + ":" + port + "/admin/purl/?maintainers=testuser";

            String errMsg = "Cannot search PURL.  Returned message was: ";
            String control = "<results><purl status=\"1\"><id>/testdomain/testPURL</id><type>302</type><maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://bbc.co.uk/</url></target></purl><purl status=\"1\"><id>/testdomain/test301PURL</id><type>301</type>" +
                    "<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://example.com/test301PURL</url></target></purl><purl status=\"1\">" +

                    "	<id>/testdomain/test302PURL</id>" +
                    "	<type>302</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://example.com/test302PURL</url></target></purl><purl status=\"1\">" +

                    "	<id>/testdomain/test303PURL</id>" +
                    "	<type>303</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<seealso><url>http://example.com/test303PURL</url></seealso></purl><purl status=\"1\">" +

                    "	<id>/testdomain/test307PURL</id>" +
                    "	<type>307</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://example.com/test307PURL</url></target></purl><purl status=\"1\">" +

                    "	<id>/testdomain/test404PURL</id>" +
                    "	<type>404</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "</purl><purl status=\"1\">" +

                    "	<id>/testdomain/test410PURL</id>" +
                    "	<type>410</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "</purl><purl status=\"1\">" +

                    "	<id>/testdomain/testClonePURL</id>" +
                    "	<type>302</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://example.com/test302PURL</url></target></purl><purl status=\"1\">" +

                    "	<id>/testdomain/testChainPURL</id>" +
                    "	<type>chain</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>/testdomain/test302PURL</url></target></purl><purl status=\"1\">" +

                    "	<id>/testdomain/testPartialPURL</id> " +
                    "	<type>partial</type>" +
                    "	<maintainers><uid>testuser</uid></maintainers>" +
                    "<target><url>http://example.com/testPartialPURL</url></target></purl></results>";

            String test = client.searchPurl(url);

            // XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        return suite;
    }

}
