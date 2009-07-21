package org.purl.test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AdvancedPurlTest extends AbstractPurlTest {
    /**
     * *************** Test Advanced PURLs *************************
     */

    // Test creating a new 301 PURL via an HTTP POST.
    public void testCreate301Purl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "301");
        formParameters.put("target", "http://example.com/test301PURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test301PURL", formParameters);

    }

    // Test resolving an existing 301 PURL via an HTTP GET.
    public void testResolve301Purl() {
        resolvePurl("/testdomain/test301PURL", "http://example.com/test301PURL");
    }


    // Test creating a new 302 PURL via an HTTP POST.
    public void testCreate302Purl() {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://example.com/test302PURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test302PURL", formParameters);
    }

    // Test resolving an existing 302 PURL via an HTTP GET.
    public void testResolve302Purl() {
        resolvePurl("/testdomain/test302PURL", "http://example.com/test302PURL");
    }


    // Test creating a new 303 PURL via an HTTP POST.
    public void testCreate303Purl() {
        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "303");
        formParameters.put("seealso", "http://example.com/test303PURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test303PURL", formParameters);

    }

    // Test resolving an existing 303 PURL via an HTTP GET.
    public void testResolve303Purl() {
        resolvePurl("/testdomain/test303PURL", "http://example.com/test303PURL");
    }


    // Test creating a new 307 PURL via an HTTP POST.
    public void testCreate307Purl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "307");
        formParameters.put("target", "http://example.com/test307PURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test307PURL", formParameters);
    }

    // Test resolving an existing 307 PURL via an HTTP GET.
    public void testResolve307Purl() {
        resolvePurl("/testdomain/test307PURL", "http://example.com/test307PURL");
    }


    // Test creating a new 404 PURL via an HTTP POST.
    public void testCreate404Purl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "404");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test404PURL", formParameters);
    }

    // Test resolving an existing 404 PURL via an HTTP GET.
    public void testResolve404Purl() {
        resolvePurl("/testdomain/test404PURL", "Not Found");
    }


    // Test creating a new 410 PURL via an HTTP POST.
    public void testCreate410Purl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "410");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/test410PURL", formParameters);
    }

    public void testResolveReservedSubDomainPurl() {
        resolvePurl("/testdomain/docs/", "Not Found");

    }

    // Test resolving an existing 410 PURL via an HTTP GET.
    public void testResolve410Purl() {
        resolvePurl("/testdomain/test410PURL", "Gone");
    }


    // Test creating a new Clone PURL via an HTTP POST.
    public void testCreateClonePurl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "clone");
        formParameters.put("basepurl", "/testdomain/test302PURL");

        assertPurlCreated("/testdomain/testClonePURL", formParameters);
    }

    // Test resolving an existing Clone PURL via an HTTP GET.
    public void testResolveClonePurl() {
        resolvePurl("/testdomain/testClonePURL", "http://example.com/test302PURL");
    }


    // Test creating a new Chain PURL via an HTTP POST.
    public void testCreateChainPurl() {

        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "chain");
        formParameters.put("basepurl", "/testdomain/test302PURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/testChainPURL", formParameters);
    }

    // Test resolving an existing Chain PURL via an HTTP GET.
    public void testResolveChainPurl() {
        resolvePurl("/testdomain/testChainPURL", "http://localhost:8080/testdomain/test302PURL");
    }


    // Test creating a new Partial PURL via an HTTP POST.
    public void testCreatePartialPurl() {


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "partial");
        formParameters.put("target", "http://example.com/testPartialPURL");
        formParameters.put("maintainers", "testuser");

        assertPurlCreated("/testdomain/testPartialPURL", formParameters);

    }

    // Test resolving an existing Partial PURL via an HTTP GET.
    public void testResolvePartialPurl() {
        resolvePurl("/testdomain/testPartialPURL/foobar", "http://example.com/testPartialPURL/foobar");
    }

    public void testResolvePartialPurlsWithParameter() {
        resolvePurl("/testdomain/testPartialPURL/foobar?blah", "http://example.com/testPartialPURL/foobar?blah");
    }
    public void testResolvePartialPurlRoot() {        
        resolvePurl("/testdomain/testPartialPURL?blah", "http://example.com/testPartialPURL?blah");
    }

    public void testPartialAppend() {
        assertPurlCreated("/testdomain/testAppend", "http://example.com/testAppend", "partial-append-extension");
        resolvePurl("/testdomain/testAppend", "http://example.com/testAppend");
        resolvePurl("/testdomain/testAppend/", "http://example.com/testAppend/");
        resolvePurl("/testdomain/testAppend/xml", "http://example.com/testAppend/xml");
        resolvePurl("/testdomain/testAppend/xml/", "http://example.com/testAppend/xml/");
        resolvePurl("/testdomain/testAppend/xml/blah", "http://example.com/testAppend/blah.xml");
        resolvePurl("/testdomain/testAppend/xml/blah/", "http://example.com/testAppend/blah.xml");


        assertPurlCreated("/testdomain/testAppend2/", "http://example.com/testAppend2/", "partial-append-extension");
        resolvePurlNotFound("/testdomain/testAppend2");
        resolvePurl("/testdomain/testAppend2/", "http://example.com/testAppend2/");
        resolvePurl("/testdomain/testAppend2/xml", "http://example.com/testAppend2/xml");
        resolvePurl("/testdomain/testAppend2/xml/", "http://example.com/testAppend2/xml/");
        resolvePurl("/testdomain/testAppend2/xml/blah", "http://example.com/testAppend2/blah.xml");
        resolvePurl("/testdomain/testAppend2/xml/blah/", "http://example.com/testAppend2/blah.xml");
        resolvePurl("/testdomain/testAppend2/xml/blah/?param=true", "http://example.com/testAppend2/blah.xml?param=true");
    }

    public void testPartialReplace() {
        assertPurlCreated("/testdomain/testReplace", "http://example.com/testReplace", "partial-replace-extension");
        resolvePurl("/testdomain/testReplace", "http://example.com/testReplace");
        resolvePurl("/testdomain/testReplace/", "http://example.com/testReplace/");
        resolvePurl("/testdomain/testReplace/xml", "http://example.com/testReplace/xml");
        resolvePurl("/testdomain/testReplace/xml/", "http://example.com/testReplace/xml/");
        resolvePurl("/testdomain/testReplace/xml/blah", "http://example.com/testReplace/blah.xml");
        resolvePurl("/testdomain/testReplace/xml/blah/", "http://example.com/testReplace/blah.xml");
        resolvePurl("/testdomain/testReplace/xml/blah.html", "http://example.com/testReplace/blah.xml");

        assertPurlCreated("/testdomain/testReplace2/", "http://example.com/testReplace2/", "partial-replace-extension");
        resolvePurlNotFound("/testdomain/testReplace2");
        resolvePurl("/testdomain/testReplace2/", "http://example.com/testReplace2/");
        resolvePurl("/testdomain/testReplace2/xml", "http://example.com/testReplace2/xml");
        resolvePurl("/testdomain/testReplace2/xml/", "http://example.com/testReplace2/xml/");
        resolvePurl("/testdomain/testReplace2/xml/blah", "http://example.com/testReplace2/blah.xml");
        resolvePurl("/testdomain/testReplace2/xml/blah/", "http://example.com/testReplace2/blah.xml");
        resolvePurl("/testdomain/testReplace2/xml/blah.html", "http://example.com/testReplace2/blah.xml");
        resolvePurl("/testdomain/testReplace2/xml/blah.html?param=true", "http://example.com/testReplace2/blah.xml?param=true");
    }


    public void testPartialIgnore() {
        assertPurlCreated("/testdomain/testIgnore/xml", "http://example.com/testIgnore?format=xml&id=", "partial-ignore-extension");
        resolvePurl("/testdomain/testIgnore/xml", "http://example.com/testIgnore?format=xml&id=");
        resolvePurl("/testdomain/testIgnore/xml/1", "http://example.com/testIgnore?format=xml&id=1");
        resolvePurl("/testdomain/testIgnore/xml/1.xml", "http://example.com/testIgnore?format=xml&id=1");
        resolvePurl("/testdomain/testIgnore/xml/1.xml?param=true", "http://example.com/testIgnore?format=xml&id=1&param=true");

        assertPurlCreated("/testdomain/testIgnore/html", "http://example.com/testIgnore/html", "partial-ignore-extension");
        resolvePurl("/testdomain/testIgnore/html", "http://example.com/testIgnore/html");
        resolvePurl("/testdomain/testIgnore/html/1", "http://example.com/testIgnore/html/1");
        resolvePurl("/testdomain/testIgnore/html/1.xml", "http://example.com/testIgnore/html/1");
        resolvePurl("/testdomain/testIgnore/html/1.xml?param=true", "http://example.com/testIgnore/html/1?param=true");

    }

}
