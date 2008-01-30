package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Provides a JUnit test suite for a PURL service.
 *
 * NB: These tests assume a clean PURL server with no data persisted.  Clean
 *     your server first to prevent possible conflicts.
 * NB: These tests asssume that automatic user registration is available in the
 *     PURL server. To turn on automatic user registration, uncomment
 *     <allowUserAutoCreation/> in src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
 * NB: Because PURL servers don't really delete resources, but merely
 *     'tombstone' them, running these tests will leave created resources
 *     on your server.
 *
 * @author David Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public class purlClientTestRunner {

  public static Test suite() {
        TestSuite suite = new TestSuite();

		/* 
			Set up.
			Create a test user, group, domain so the other tests can proceed.
		*/
        suite.addTest(new simplePurlClientTest("testRegisterUser"));
        suite.addTest(new simplePurlClientTest("testRegisterUser2"));
		suite.addTest(new simplePurlClientTest("testLoginUser"));
        suite.addTest(new simplePurlClientTest("testCreateGroup"));
        suite.addTest(new simplePurlClientTest("testCreateDomain"));

// TODO?: Add tests to determine whether <allowUserAutoCreation/> has been set
//       and approve/deny users as necessary.  Do the same with domains.
		
		/*
			Modify the user, group and domain
		*/
        suite.addTest(new simplePurlClientTest("testModifyUser"));
        suite.addTest(new simplePurlClientTest("testModifyGroup"));
        suite.addTest(new simplePurlClientTest("testModifyDomain"));

		/*
			Search the user, group and domain.
		*/
        suite.addTest(new simplePurlClientTest("testSearchUser"));
        suite.addTest(new simplePurlClientTest("testSearchGroup"));
        suite.addTest(new simplePurlClientTest("testSearchDomain"));

		/*
			Single PURL actions
		*/
        suite.addTest(new simplePurlClientTest("testCreatePurl"));
        suite.addTest(new simplePurlClientTest("testRecreatePurl"));
        suite.addTest(new simplePurlClientTest("testModifyPurl"));
        suite.addTest(new simplePurlClientTest("testSearchPurl"));
        suite.addTest(new simplePurlClientTest("testValidatePurl"));
        suite.addTest(new simplePurlClientTest("testResolvePurl"));
        suite.addTest(new simplePurlClientTest("testDeletePurl"));

		/*
			Advanced PURL actions. Add, resolve and delete PURLs of each type.
		*/
        suite.addTest(new simplePurlClientTest("testCreate301Purl"));
        suite.addTest(new simplePurlClientTest("testResolve301Purl"));

        suite.addTest(new simplePurlClientTest("testCreate302Purl"));
        suite.addTest(new simplePurlClientTest("testResolve302Purl"));

        suite.addTest(new simplePurlClientTest("testCreate303Purl"));
        suite.addTest(new simplePurlClientTest("testResolve303Purl"));

        suite.addTest(new simplePurlClientTest("testCreate307Purl"));
        suite.addTest(new simplePurlClientTest("testResolve307Purl"));

        suite.addTest(new simplePurlClientTest("testCreate404Purl"));
        suite.addTest(new simplePurlClientTest("testResolve404Purl"));

        suite.addTest(new simplePurlClientTest("testCreate410Purl"));
        suite.addTest(new simplePurlClientTest("testResolve410Purl"));

        suite.addTest(new simplePurlClientTest("testCreateClonePurl"));
        suite.addTest(new simplePurlClientTest("testResolveClonePurl"));

        suite.addTest(new simplePurlClientTest("testCreateChainPurl"));
        suite.addTest(new simplePurlClientTest("testResolveChainPurl"));

        suite.addTest(new simplePurlClientTest("testCreatePartialPurl"));
        suite.addTest(new simplePurlClientTest("testResolvePartialPurl"));

        suite.addTest(new simplePurlClientTest("testDelete301Purl"));
        suite.addTest(new simplePurlClientTest("testDelete302Purl"));
        suite.addTest(new simplePurlClientTest("testDelete303Purl"));
        suite.addTest(new simplePurlClientTest("testDelete307Purl"));
        suite.addTest(new simplePurlClientTest("testDelete404Purl"));
        suite.addTest(new simplePurlClientTest("testDelete410Purl"));
        suite.addTest(new simplePurlClientTest("testDeleteClonePurl"));
        suite.addTest(new simplePurlClientTest("testDeleteChainPurl"));
        suite.addTest(new simplePurlClientTest("testDeletePartialPurl"));

		/*
			Batch PURL actions
		*/
        suite.addTest(new simplePurlClientTest("testCreate301Purls"));
        suite.addTest(new simplePurlClientTest("testCreate302Purls"));
        suite.addTest(new simplePurlClientTest("testCreate303Purls"));
        suite.addTest(new simplePurlClientTest("testCreate307Purls"));
        suite.addTest(new simplePurlClientTest("testCreate404Purls"));
        suite.addTest(new simplePurlClientTest("testCreate410Purls"));
        suite.addTest(new simplePurlClientTest("testCreateClonePurls"));
        suite.addTest(new simplePurlClientTest("testCreateChainPurls"));
        suite.addTest(new simplePurlClientTest("testCreatePartialRedirectPurls"));
        suite.addTest(new simplePurlClientTest("testCreatePurls"));
        suite.addTest(new simplePurlClientTest("testModifyPurls"));
		suite.addTest(new simplePurlClientTest("testValidatePurls"));
		
		/*
			Remove the test user, group and domain.
		*/
        suite.addTest(new simplePurlClientTest("testDeleteDomain"));
        suite.addTest(new simplePurlClientTest("testDeleteGroup"));
        suite.addTest(new simplePurlClientTest("testDeleteUser"));
        suite.addTest(new simplePurlClientTest("testDeleteUser2"));

        return suite;
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

}