package org.purl.test;

import java.net.*;
import java.io.*;
import java.util.*;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.util.*;
import org.restlet.resource.*;
import org.restlet.data.Form;

/**
 * Attempts to load 1 million PURLs into a PURL server.
 *
 * @author David Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public final class millionPurlLoadTest {
    	
	// Set the host and port for all subsequent URLs.
	private String host = "localhost";
	private String port = "8080";
	private simplePurlClient client;
	
	// Constructor
	public millionPurlLoadTest () {
		client = new simplePurlClient();
	}
	
	/**
	  * Attempts to load 1 million PURLs
	  */
	public static void main(String args[]) {
		
		millionPurlLoadTest harness = new millionPurlLoadTest();
		int delay = 900000;  // Default delay 15 minutes between loads.
		
		if ( null != args && args.length == 2 ) {
			if ( args[0].equals("-d") ) {
				delay = Integer.parseInt(args[1]);
			}
		}
		
		System.out.println("Delaying " + delay + " millseconds between loads.");
		
		// Create a user.
		String userResult = harness.registerUser("testuser", "Test User", "Zepheira", "test.user@example.com", "Testing!", "testing", "Testing.");
		System.out.println("Result of user registration: " + userResult);
		
		// Write test files to the filesystem, if needed.
		if ( ! harness.checkFiles() ) {
			harness.writeFiles();
		}
				
		for ( int i=1; i<=1000; i++ ) {
			// Add 1,000 PURLs at a time via the batch interface.
			harness.createPurls("loadtest_" + i);
			System.out.println("PURLs created: " + (i * 1000) );
			try {
				//int delay = delayBase + (i * 10000);
				Thread.sleep(delay);
			} catch (Exception e2) {
				System.err.println("Error: " + e2.getMessage() );
			}
			
		}
	}


	/** Create a batch of PURLs via an HTTP POST.
	  *
	  * @param filename The name of a file in the "testdata" directory that holds input data.
	  */
	public String createPurls(String filename) {

		try {
			String url = "http://" + host + ":" + port + "/admin/purls/";

			File file = new File("testdata" + 
								System.getProperty("file.separator") +
								"large" +
								System.getProperty("file.separator") +
								filename);

			String test = client.createPurls(url, file);
			
			return test;
			
		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}
		
		return "ERROR: Test not completed.";
	}		

	
	/** Register a new user via an HTTP POST.
	  * 
	  * NB: Assumes that immediate user registration is available in the PURL server.
	  *     To turn on immediate user registration, unset <allowUserAutoCreation/>
	  *     in src/mod-purl-admin/modules/mod-purl-admin/etc/PURLConfig.xml
	  *
	  * @param uid A user id (e.g. "dwood").
	  * @param name The name of the user (e.g. "David Wood").
	  * @param affiliation The user's affiliation (such as a company name).
 	  * @param email The user's email address
	  * @param passwd The user's new password (to create the account; a password is not required to authenticate yet).
	  * @param hint A hint that they user can later user to remember their password.
	  * @param justification A justification as to why the account should be created (some PURL servers require this).
	*/
	public String registerUser(String uid, String name, String affiliation, String email, String passwd, String hint, String justification) {

		try {
			String url = "http://" + host + ":" + port + "/admin/user/" + uid;

			Map<String, String> formParameters = new HashMap<String, String>();
			formParameters.put("name", name);
			formParameters.put("affiliation", affiliation);
			formParameters.put("email", email);
			formParameters.put("passwd", passwd);
			formParameters.put("hint", hint);
			formParameters.put("justification", justification);

			String test = client.registerUser(url, formParameters);
			
			return test;

		} catch (Exception e) {
			reportException("Failed to resolve URL: ", e);
		}

		return "ERROR: Test not completed.";
	}
	

	// Handle any exceptions encountered above.  For JUnit tests, it
	// is enough to call JUnit's fail() method.
	private void reportException(String message, Exception e) {
		System.err.println( message + e.getMessage() + " : " + e.getCause() );
	}

	// Check to see if the last test file exists.
	private boolean checkFiles() {
		
		File file = new File("testdata" + 
							System.getProperty("file.separator") +
							"large" +
							System.getProperty("file.separator") +
							"loadtest_1000");
		boolean exists = file.exists();
		
		return exists;
	}
	
	// Write 1000 test files, each containing 1000 PURLs.
	private void writeFiles() {
		
		for ( int i=0; i<1000; i++ ) {
			
			String filename = "loadtest_" + (i + 1);
			
			try	{
			    // Open an output stream
			    FileOutputStream fileout = new FileOutputStream ("testdata" + 
									System.getProperty("file.separator") +
									"large" +
									System.getProperty("file.separator") +
									filename);

			    // Print the PURL batch XML structure to each file.
			    PrintStream fileprint = new PrintStream(fileout);
				fileprint.println ("<purls>");
				for ( int j=1; j<=1000; j++ ) {
					int purlNum = (i*1000) + j;
					fileprint.println ("  <purl id=\"/testdomain/test_" + purlNum + "\" type=\"302\">");
					fileprint.println ("    <maintainers>");
					fileprint.println ("      <maintainer id=\"testuser\"/>");
					fileprint.println ("    </maintainers>");
					fileprint.println ("    <target url=\"http://example.com/testPURL_" + purlNum + "\"/>");
					fileprint.println ("  </purl>");
				}
				fileprint.println ("</purls>");

			    // Close the print and output stream
				fileprint.close();
			    fileout.close();		
			}
			// Catches any error conditions
			catch (IOException e) {
				System.err.println ("Unable to write to file " + filename);
				System.exit(-1);
			}
		}
	}


} // class
