package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Provides a JUnit test suite for a PURL service.
 *
 * @author David Hyland-Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public class purlClientTestRunner {

  public static Test suite() {
        TestSuite suite = new TestSuite();

		// User actions
        suite.addTest(new simplePurlClientTest("testRegisterUser"));
        suite.addTest(new simplePurlClientTest("testModifyUser"));
        suite.addTest(new simplePurlClientTest("testSearchUser"));
        suite.addTest(new simplePurlClientTest("testDeleteUser"));

		// Group actions
        suite.addTest(new simplePurlClientTest("testRegisterGroup"));
        suite.addTest(new simplePurlClientTest("testModifyGroup"));
        suite.addTest(new simplePurlClientTest("testSearchGroup"));
        suite.addTest(new simplePurlClientTest("testDeleteGroup"));

		// Domain actions
        suite.addTest(new simplePurlClientTest("testRegisterDomain"));
        suite.addTest(new simplePurlClientTest("testModifyDomain"));
        suite.addTest(new simplePurlClientTest("testSearchDomain"));
        suite.addTest(new simplePurlClientTest("testDeleteDomain"));

		// Single PURL actions
        suite.addTest(new simplePurlClientTest("testRegisterPurl"));
        suite.addTest(new simplePurlClientTest("testModifyPurl"));
        //suite.addTest(new simplePurlClientTest("testSearchPurl"));
        //suite.addTest(new simplePurlClientTest("testValidatePurl"));
        suite.addTest(new simplePurlClientTest("testResolvePurl"));
        suite.addTest(new simplePurlClientTest("testDeletePurl"));

		// Batch PURL actions
        //suite.addTest(new simplePurlClientTest("testRegisterPurls"));
        //suite.addTest(new simplePurlClientTest("testModifyPurls"));
        //suite.addTest(new simplePurlClientTest("testSearchPurls"));
        //suite.addTest(new simplePurlClientTest("testValidatePurls"));
        //suite.addTest(new simplePurlClientTest("testDeletePurls"));

        return suite;
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

}