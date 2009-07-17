package org.purl.test;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class AbstractPurlTest extends AbstractIntegrationTest {

    /** Resolve a PURL via HTTP GET.
      * @param path A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
      * @param control An expected response string from the resolution.  Generally, this will be a URL from a PURL's Location header.
    */
    public void resolvePurl(String path, String control) {

        try {
            String url = "http://" + host + ":" + port + path;

            String errMsg = "Cannot resolve PURL: ";
            String test = client.resolvePurl(url);

            // Convert the test and control values to lower case for clean comparison.
            if ( test != null ) {
                test = test.toLowerCase();
            }
            if ( control != null ) {
                control = control.toLowerCase();
            }
                        
            // Textual response, so use assertEquals().
            assertEquals(errMsg + test, control, test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

    public void resolvePurlNotFound(String path) {
         try {
            String url = "http://" + host + ":" + port + path;


            String test = client.resolvePurl(url);
             
            // Textual response, so use assertEquals().
            assertEquals("Not Found", test);

        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

   public void resolvePurlMetdata(String path) throws Exception {
       String url1 = "http://" + host + ":" + port + "/purl" + path;
       String url2 = "http://" + host + ":" + port + "/admin/purl" + path; 
       assertEquals(client.searchPurl(url1), client.searchPurl(url2));
   }





}
