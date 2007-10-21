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

		// Set up.
		// Create a test user, group, domain so the other tests can proceed.
        suite.addTest(new simplePurlClientTest("testRegisterUser"));
        suite.addTest(new simplePurlClientTest("testCreateGroup"));
        suite.addTest(new simplePurlClientTest("testCreateDomain"));
		
		// Modify the user, group and domain
        //READY suite.addTest(new simplePurlClientTest("testModifyUser"));
        //READY suite.addTest(new simplePurlClientTest("testModifyGroup"));
        //READY suite.addTest(new simplePurlClientTest("testModifyDomain"));

		// Search the user, group and domain.
        suite.addTest(new simplePurlClientTest("testSearchUser"));
        suite.addTest(new simplePurlClientTest("testSearchGroup"));
        suite.addTest(new simplePurlClientTest("testSearchDomain"));

		// Single PURL actions
        suite.addTest(new simplePurlClientTest("testCreatePurl"));
        // READY? Check with Brian re 'param2 missing arg. suite.addTest(new simplePurlClientTest("testModifyPurl"));
        // READY: suite.addTest(new simplePurlClientTest("testSearchPurl"));
        // NOTYET: suite.addTest(new simplePurlClientTest("testValidatePurl"));
        suite.addTest(new simplePurlClientTest("testResolvePurl"));
        suite.addTest(new simplePurlClientTest("testDeletePurl"));

		// Batch PURL actions
        // READY, but fix control var: suite.addTest(new simplePurlClientTest("testCreatePurls"));
        // NOTYET?, but fix control var and purlsmodify file: suite.addTest(new simplePurlClientTest("testModifyPurls"));

		// Remove the test user, group and domain.
        suite.addTest(new simplePurlClientTest("testDeleteDomain"));
        suite.addTest(new simplePurlClientTest("testDeleteGroup"));
        suite.addTest(new simplePurlClientTest("testDeleteUser"));

        return suite;
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

}