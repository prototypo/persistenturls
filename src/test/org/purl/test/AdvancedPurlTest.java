package org.purl.test;

/**
 *
 */
public class AdvancedPurlTest extends AbstractPurlTest {
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


	// Test creating a new 302 PURL via an HTTP POST.
	public void testCreate302Purl() {
		createEasyPurl("/testdomain/test302PURL", "302", "testuser", "http://example.com/test302PURL");
	}

	// Test resolving an existing 302 PURL via an HTTP GET.
	public void testResolve302Purl() {
		resolvePurl("/testdomain/test302PURL", "http://example.com/test302PURL");
	}



	// Test creating a new 303 PURL via an HTTP POST.
	public void testCreate303Purl() {
		createPurl("/testdomain/test303PURL", "303", "testuser", null, "http://example.com/test303PURL", false);
	}

	// Test resolving an existing 303 PURL via an HTTP GET.
	public void testResolve303Purl() {
		resolvePurl("/testdomain/test303PURL", "http://example.com/test303PURL");
	}


	// Test creating a new 307 PURL via an HTTP POST.
	public void testCreate307Purl() {
		createEasyPurl("/testdomain/test307PURL", "307", "testuser", "http://example.com/test307PURL");
	}

	// Test resolving an existing 307 PURL via an HTTP GET.
	public void testResolve307Purl() {
		resolvePurl("/testdomain/test307PURL", "http://example.com/test307PURL");
	}



	// Test creating a new 404 PURL via an HTTP POST.
	public void testCreate404Purl() {
		createEasyPurl("/testdomain/test404PURL", "404", "testuser", null);
	}

	// Test resolving an existing 404 PURL via an HTTP GET.
	public void testResolve404Purl() {
		resolvePurl("/testdomain/test404PURL", "Not Found");
	}



	// Test creating a new 410 PURL via an HTTP POST.
	public void testCreate410Purl() {
		createEasyPurl("/testdomain/test410PURL", "410", "testuser", null);
	}

	// Test resolving an existing 410 PURL via an HTTP GET.
	public void testResolve410Purl() {
		resolvePurl("/testdomain/test410PURL", "Gone");
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



	// Test creating a new Chain PURL via an HTTP POST.
	public void testCreateChainPurl() {
		createPurl("/testdomain/testChainPURL", "chain", "testuser", null, null, true);
	}

	// Test resolving an existing Chain PURL via an HTTP GET.
	public void testResolveChainPurl() {
		resolvePurl("/testdomain/testChainPURL", "http://localhost:8080/testdomain/test302PURL");
	}



	// Test creating a new Partial PURL via an HTTP POST.
	public void testCreatePartialPurl() {
		createEasyPurl("/testdomain/testPartialPURL", "partial", "testuser", "http://example.com/testPartialPURL");
	}

	// Test resolving an existing Partial PURL via an HTTP GET.
	public void testResolvePartialPurl() {
		resolvePurl("/testdomain/testPartialPURL/foobar", "http://example.com/testPartialPURL/foobar");
	}


}
