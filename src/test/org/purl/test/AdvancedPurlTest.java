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

        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test301PURL</id>" +
                "<type>301</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://example.com/test301PURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "301");
        formParameters.put("target", "http://example.com/test301PURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test301PURL", formParameters, control, true);

    }

    // Test resolving an existing 301 PURL via an HTTP GET.
    public void testResolve301Purl() {
        resolvePurl("/testdomain/test301PURL", "http://example.com/test301PURL");
    }


    // Test creating a new 302 PURL via an HTTP POST.
    public void testCreate302Purl() {

        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test302PURL</id>" +
                "<type>302</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://example.com/test302PURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "302");
        formParameters.put("target", "http://example.com/test302PURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test302PURL", formParameters, control, true);
    }

    // Test resolving an existing 302 PURL via an HTTP GET.
    public void testResolve302Purl() {
        resolvePurl("/testdomain/test302PURL", "http://example.com/test302PURL");
    }


    // Test creating a new 303 PURL via an HTTP POST.
    public void testCreate303Purl() {
        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test303PURL</id>" +
                "<type>303</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<seealso><url>http://example.com/test303PURL</url></seealso></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "303");
        formParameters.put("seealso", "http://example.com/test303PURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test303PURL", formParameters, control, true);

    }

    // Test resolving an existing 303 PURL via an HTTP GET.
    public void testResolve303Purl() {
        resolvePurl("/testdomain/test303PURL", "http://example.com/test303PURL");
    }


    // Test creating a new 307 PURL via an HTTP POST.
    public void testCreate307Purl() {


        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test307PURL</id>" +
                "<type>307</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://example.com/test307PURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "307");
        formParameters.put("target", "http://example.com/test307PURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test307PURL", formParameters, control, true);
    }

    // Test resolving an existing 307 PURL via an HTTP GET.
    public void testResolve307Purl() {
        resolvePurl("/testdomain/test307PURL", "http://example.com/test307PURL");
    }


    // Test creating a new 404 PURL via an HTTP POST.
    public void testCreate404Purl() {


        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test404PURL</id>" +
                "<type>404</type>" +
                "<maintainers><uid>testuser</uid></maintainers></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "404");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test404PURL", formParameters, control, true);
    }

    // Test resolving an existing 404 PURL via an HTTP GET.
    public void testResolve404Purl() {
        resolvePurl("/testdomain/test404PURL", "Not Found");
    }


    // Test creating a new 410 PURL via an HTTP POST.
    public void testCreate410Purl() {
        String control = "<purl status=\"1\">" +
                "<id>/testdomain/test410PURL</id>" +
                "<type>410</type>" +
                "<maintainers><uid>testuser</uid></maintainers></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "410");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/test410PURL", formParameters, control, true);
    }

    // Test resolving an existing 410 PURL via an HTTP GET.
    public void testResolve410Purl() {
        resolvePurl("/testdomain/test410PURL", "Gone");
    }


    // Test creating a new Clone PURL via an HTTP POST.
    public void testCreateClonePurl() {
        String control = "<purl status=\"1\">" +
                "<id>/testdomain/testClonePURL</id>" +
                "<type>302</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://example.com/test302PURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "clone");
        formParameters.put("basepurl", "/testdomain/test302PURL");

        createPurl("/testdomain/testClonePURL", formParameters, control, true);
    }

    // Test resolving an existing Clone PURL via an HTTP GET.
    public void testResolveClonePurl() {
        resolvePurl("/testdomain/testClonePURL", "http://example.com/test302PURL");
    }


    // Test creating a new Chain PURL via an HTTP POST.
    public void testCreateChainPurl() {

        String control = "<purl status=\"1\">" +
                "<id>/testdomain/testChainPURL</id>" +
                "<type>chain</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>/testdomain/test302PURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "chain");
        formParameters.put("basepurl", "/testdomain/test302PURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/testChainPURL", formParameters, control, true);
    }

    // Test resolving an existing Chain PURL via an HTTP GET.
    public void testResolveChainPurl() {
        resolvePurl("/testdomain/testChainPURL", "http://localhost:8080/testdomain/test302PURL");
    }


    // Test creating a new Partial PURL via an HTTP POST.
    public void testCreatePartialPurl() {

        String control = "<purl status=\"1\">" +
                "<id>/testdomain/testPartialPURL</id>" +
                "<type>partial</type>" +
                "<maintainers><uid>testuser</uid></maintainers>" +
                "<target><url>http://example.com/testPartialPURL</url></target></purl>";


        Map<String, String> formParameters = new HashMap<String, String>();
        formParameters.put("type", "partial");
        formParameters.put("target", "http://example.com/testPartialPURL");
        formParameters.put("maintainers", "testuser");

        createPurl("/testdomain/testPartialPURL", formParameters, control, true);

    }

    // Test resolving an existing Partial PURL via an HTTP GET.
    public void testResolvePartialPurl() {
        resolvePurl("/testdomain/testPartialPURL/foobar", "http://example.com/testPartialPURL/foobar");
    }


}
