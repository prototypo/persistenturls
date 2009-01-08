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



	// Convenience method for creating PURLs of types 301, 302, 307, 404 and 410.
	public void createEasyPurl(String path, String type, String maintainers, String target) {
		createPurl(path, type, maintainers, target, null, false);
	}

	// Convenience method for creating PURLs which are expected to succeed.
	public void createPurl(String path, String type, String maintainers, String target, String seealso, boolean useBasepurl) {
		createPurl( path,  type,  maintainers,  target,  seealso,  useBasepurl, true);
	}

	/** Create a new PURL via an HTTP POST.
	  * @param path A PURL path or id (starting with a '/' and containing its domains and name, e.g. /testdomain/subdomain/purlName).
	  * @param type The type of PURL (one of "301", "302", "303", "307", "404", "410", "clone", "chain", "partial").
	  * @param maintainers A comma-separated list of user or group names that are allowed to maintain the named PURL.
	  * @param target A URL to redirect to; used for PURLs of type 301, 302, 307 and partial.
	  * @param seealso A URL for See Also requests; used for PURLs of type 303.
	  * @param useBasepurl Whether or not to include a default basePURL for testing of PURLs of types clone or chain.
	  * @param expectSuccess Whether the test should expect to succeed.  If false, it will expect to fail.
	*/
	public void createPurl(String path, String type, String maintainers, String target, String seealso, boolean useBasepurl, boolean expectSuccess) {

		try {
			String url = "http://" + host + ":" + port + "/admin/purl" + path;
			String control = "<purl><id>" + path + "</id>";
			if ( type.equals("clone") ) {
				// For chaining and cloning PURLs.
				// NB: Presumes use of the hard-coded test data!
				control += "<type>302</type>";
			} else {
				// For most types of PURLS.
				control += "<type>" + type + "</type>";
			}

			Map<String, String> formParameters = new HashMap<String,String>();
			formParameters.put("type", type);

			if ( maintainers != null ) {
				formParameters.put("maintainers", maintainers);
				control += "<maintainers><uid>" + maintainers + "</uid></maintainers>";
			}
			if ( target != null ) {
				formParameters.put("target", target);
				control += "<target><url>" + target + "</url></target>";
			}
			if ( seealso != null ) {
				formParameters.put("seealso", seealso);
				control += "<seealso><url>" + seealso + "</url></seealso>";
			}
			if ( type == "clone" ) {
				// For cloning.
				// Note use of hardcoded basePURL, which must be created first.
				formParameters.put("basepurl", "/testdomain/test302PURL");
				control += "<target><url>http://example.com/test302PURL</url></target><maintainers><uid>testuser</uid></maintainers>";
			} else if ( type == "chain" ) {
				// For chaining.
				// Note use of hardcoded basePURL, which must be created first.
				formParameters.put("basepurl", "/testdomain/test302PURL");
				control += "<target><url>/testdomain/test302PURL</url></target>";
			}
			control += "</purl>";

			String errMsg = "Cannot create a new " + type + " PURL: ";

			String test = client.createPurl(url, formParameters);
            System.out.println(test);

			// Convert the test and control values to lower case for clean comparison.
			//String testLC = test.toLowerCase();
			//String controlLC = control.toLowerCase();

			if (expectSuccess) {
				// This test expects to succeed.
				try {
					// XML response, so use assertXMLEqual.
		            XMLUnit.setIgnoreWhitespace(true);
                    
					XMLAssert.assertXMLEqual(errMsg + test, control, test);
				} catch (Throwable e){
					// XMLUnit can throw an error if the test or control are not XML.
					// Unfortunately, the error is very poorly described
					// ("Content is not allowed in prolog."), so we change
					// it here to avoid confusion.
					throw new Exception ("Bad input.  XMLUnit expected XML, but received plain text.");
				}
			} else {
				// This test expects to fail.
				// Override the control text:
				control = "PURL: " + path + " already exists.";
				//controlLC = control.toLowerCase();
				// Textual response, so use assertEquals().
				assertEquals(errMsg, control, test);
			}

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
	}


    protected void createPurl(String path, Map<String, String> formParameters, String control, boolean isXML) {
        try {
            String url = "http://" + host + ":" + port + "/admin/purl" + path;


            String errMsg = "Cannot create a new " + formParameters.get("type") + " PURL: ";
            String test = client.createPurl(url, formParameters);
            if (isXML) {
                XMLUnit.setIgnoreWhitespace(true);
                XMLAssert.assertXMLEqual(errMsg + test, control, test);
            } else {
                assertEquals(errMsg, control, test);
            }
        } catch (Exception e) {
            reportException("Failed to resolve URL: ", e);
        }
    }

}
