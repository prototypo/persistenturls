package org.purl.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Provides a JUnit test suite for a PURL service.
 * <p/>
 * NB: These tests assume a clean PURL server with no data persisted.  Clean
 * your server first to prevent possible conflicts.
 * NB: These tests asssume that automatic user registration is available in the
 * PURL server. To turn on automatic user registration, uncomment
 * <allowUserAutoCreation/> in src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
 * NB: Because PURL servers don't really delete resources, but merely
 * 'tombstone' them, running these tests will leave created resources
 * on your server.
 *
 * @author David Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public class PurlIntegrationTestRunner {

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(RegisterUserTest.class);
        suite.addTestSuite(CreateGroupTest.class);
        suite.addTestSuite(CreateDomainTest.class);
        suite.addTestSuite(ModifyUserTest.class);
        suite.addTestSuite(ModifyGroupTest.class);
        suite.addTestSuite(ModifyDomainTest.class);

        suite.addTestSuite(SinglePurlTest.class);
        suite.addTestSuite(AdvancedPurlTest.class);
        suite.addTestSuite(BatchPurlTest.class);


        suite.addTestSuite(SearchTest.class);

        suite.addTestSuite(HierarchicalGroupTest.class);

        suite.addTestSuite(DeleteTest.class);
        suite.addTestSuite(DeleteUserTest.class);

        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

}