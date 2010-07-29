package org.purl.test;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.io.File;

/**
 *
 */
public class BatchPurlTest extends AbstractIntegrationTest {



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

    // TODO: Implement batch modify and validate on server:

//    // Test modifying a batch of PURLs via an HTTP PUT.
//    public void testModifyPurls() {
//
//        try {
//            String url = "http://" + host + ":" + port + "/admin/purls/";
//
//            File file = new File(getTestDataFile("purlsmodify.xml"));
//
//            String errMsg = "Cannot modify a batch of PURLs: ";
//            // NB: Change the number below (6) if the number of PURLs defined in the input file changes.
//            String control = "<purl-batch-success numCreated=\"6\"/>";
//            String test = client.modifyPurls(url, file);
//
//            // XML response, so use assertXMLEqual.
//            XMLUnit.setIgnoreWhitespace(true);
//            XMLAssert.assertXMLEqual(errMsg + test, control, test);
//
//        } catch (Exception e) {
//            reportException("Failed to resolve URL: ", e);
//        }
//    }
//
//    // Test modifying a batch of PURLs via an HTTP PUT.
//    public void testValidatePurls() {
//
//        try {
//            String url = "http://" + host + ":" + port + "/admin/targeturls/";
//
//            File file = new File(getTestDataFile("purlsvalidate.xml"));
//            String errMsg = "Cannot validate a batch of PURLs: ";
//            String control = readFile(getTestDataFile("purlsvalidatecontrol.xml"));
//            String test = client.validatePurls(url, file);
//
//            // XML response, so use assertXMLEqual.
//            XMLUnit.setIgnoreWhitespace(true);
//            XMLAssert.assertXMLEqual(errMsg + test, control, test);
//
//        } catch (Exception e) {
//            reportException("Failed to resolve URL: ", e);
//        }
//    }


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
			String control = "<purl-batch total=\"" + numCreated + "\" numCreated=\"" + numCreated + "\" failed=\"0\"></purl-batch>";
			String test = client.createPurls(url, file);

			// XML response, so use assertXMLEqual.
            XMLUnit.setIgnoreWhitespace(true);
			XMLAssert.assertXMLEqual(errMsg + test, control, test);

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}

}
