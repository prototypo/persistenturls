package org.purl.test;

import junit.framework.TestCase;

import java.io.*;

/**
 *
 */
public class AbstractIntegrationTest extends TestCase {

	protected PurlTestClient client = null;
    // Set the host and port for all subsequent URLs.
	protected String host = "localhost";
	protected String port = "8080";

    // Handle any exceptions encountered above.  For JUnit tests, it
	// is enough to call JUnit's fail() method.
	protected void reportException(String message, Exception e) {
		fail( message + e.getMessage() + " : " + e.getCause() );
	}

	protected void reportResult(String testName, String testResult) {
		//System.err.println("\n\nTEST RESULT FOR " + testName + ":\n" + testResult);

	}

    // Create an instance of the PURL test client for all methods to use.
	protected void setUp() {
        if (client == null) {
	        client = new PurlTestClient();
        }
	}


	protected String getTestDataFile(String filename) {
        String separator = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");

        StringBuffer sb = new StringBuffer(System.getProperty("user.dir"));
        sb.append(separator);

        if(!userDir.endsWith("test")) {
            sb.append("test");
            sb.append(separator);
        }

        sb.append("testdata");
        sb.append(separator);
        sb.append(filename);
        return sb.toString();
	}

	/** Read in the contents of a file and return them.
	  *
	  * @param filename The name of a file to read.
	  * @return The contents of the file.
	  */
    protected static String readFile(String filename) throws FileNotFoundException, IOException {

		File file = new File(filename);
		String content = "";
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		fis = new FileInputStream(file);
		bis = new BufferedInputStream(fis);
		dis = new DataInputStream(bis);

		while (dis.available() != 0) {
			content += dis.readLine();
		}

		fis.close();
		bis.close();
		dis.close();

		return content;
    }

}
