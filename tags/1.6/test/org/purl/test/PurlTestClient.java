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
 * Provides a RESTful test harness for a PURL service.
 * Also serves as example code demonstrating the PURL client API.
 *
 * @author David Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public final class PurlTestClient {
    
	// Instance vars:
	// A single RESTlet client for all HTTP transactions.
	public Client client;
	
	// The session cookie to be set by the server in login().
	String cookie;
	
	// Constructor
	public PurlTestClient() {
		client = new Client(Protocol.HTTP);
	}

    /****************** Log in **************************/
	
	/**
	 * Log in a registered user via an HTTP POST.
	 *
	 * @param  url A URL addressing a login service for PURLs.
	 * @param  formParameters Parameters to the login service (id, password, referrer).
	 * @return The response from the server.
	 */
	public String login (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.
		Response response = client.post(url, rep);
		
		// Retain the session cookie set by the server.
		cookie = response.getCookieSettings().getValues("NETKERNELSESSION");
		
		CookieFactory.setCookie(cookie);

		// DBG
		//System.out.println("Cookie returned from server: " + cookie);
		
		String output = response.getEntity().getText();

        response = null ; System.gc();
		return output;
	}
	
	/**
	 * Log out a registered user via an HTTP POST.
	 *
	 * @param  url A URL addressing a logout service for PURLs.
	 * @return The response from the server.
	 */
	public String logout (String url) throws IOException {
		
		// Request the resource and return its textual content.
		Response response = client.post(url, null);
		
		// Retain the session cookie set by the server.
		cookie = response.getCookieSettings().getValues("NETKERNELSESSION");
		
		CookieFactory.setCookie(cookie);
		
		// DBG
		//System.out.println("Cookie returned from server: " + cookie);
		
		String output = response.getEntity().getText();
		response = null ; System.gc();
		return output;
	}

    /**
     * Fetch the login status
     * @param url
     * @return
     * @throws IOException
     */
    public String loginstatus(String url) throws IOException {
        return handleRequest(url, Method.GET,  null);
		
    }

	/****************** Single PURLs **************************/
	
	/**
	 * Create a new PURL via an HTTP POST.
	 *
	 * @param  url A URL addressing a creation service for PURLs.
	 * @return The response from the server.
	 */
	public String createPurl (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		return handleRequest(url, Method.POST, rep);
	}
	
	/**
	 * Modify an existing PURL via an HTTP PUT.
	 *
	 * @param url A URL addressing a modify service for PURLs.
	 * @param formParameters An XML file containing the new PURL parameters.
	 * @return The response from the server.
	 */
	public String modifyPurl (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		return handleRequest(url + "?" + form, Method.PUT, null);
		
	}

	/**
	 * Search for PURLs via an HTTP GET.
	 *
	 * @param url A URL addressing a search service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchPurl(String url) throws IOException {
        return doSimpleGet(url);
	}

	/**
	 * Validate an existing PURL via an HTTP GET.
	 *
	 * @param url A URL addressing a validation service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String validatePurl(String url) throws IOException {
        return doSimpleGet(url);
	}

	/**
	 * Resolve an existing PURL via an HTTP GET.
	 *
	 * @param url A URL addressing a validation service for PURLs.
	 * @return The Location header from the server, if provided, or the status description if not.
	 */
	public String resolvePurl(String url) throws IOException {
		
		Response response = client.get(url);
		Map<String,Object> responseAttrs = response.getAttributes();
		String result = null;
		try {		
			Form headersForm = (Form) responseAttrs.get( "org.restlet.http.headers" );
			result = headersForm.getFirst("Location").getValue();


		} catch (Exception e){
			// Map<K,V>.get() will throw a NullPointerException if it
			// can't find the key.  This will occur for PURLs of type 404 and 410.
			// In that case, we return the HTTP status description (e.g. "Gone").
			result = response.getStatus().getDescription();
		}
        response = null;
		System.gc();
        return result;
	}

	/**
	 * Delete an existing PURL via an HTTP DELETE.
	 *
	 * @param  url A URL addressing a validation service for PURLs.
	 * @return The response from the server.
	 */
	public String deletePurl (String url) throws IOException {
		
		return handleRequest(url, Method.DELETE, null);
	}
	
	
	/****************** Batch PURLs **************************/
	
	/**
	 * Create a batch of PURLs via an HTTP POST.
	 *
	 * @param  url a String representation of a URL to a batch creation service.
	 * @return The response from the server in XML.
	 */
	public String createPurls (String url, File file) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.TEXT_XML, 3600 );
		return handleRequest(url, Method.POST, rep);
	}
	
	/**
	 * Modify a batch of PURLs via an HTTP PUT.
	 *
	 * @param  url a String representation of a URL to a batch modification service.
	 * @return The response from the server in XML.
	 */
	public String modifyPurls (String url, File file) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		return handleRequest(url, Method.PUT, rep);
	}

	/**
	 * Validate a batch of PURLs via an HTTP PUT.
	 *
	 * @param  url a String representation of a URL to a batch validation service.
	 * @return The response from the server in XML.
	 */
	public String validatePurls (String url, File file) throws IOException {

		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		return handleRequest(url, Method.PUT, rep);
	}
	
	/****************** Users **************************/
	
	/**
	 * Register a new user via an HTTP POST.
	 *
	 * @param  url A URL addressing a user registration service.
	 * @return The response from the server.
	 */
	public String registerUser (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.
        Response response = client.post(url, rep);
        String result = response.getEntity().getText();
        response = null ; System.gc();
        return result;
	}
	
	/**
	 * Modify an existing user via an HTTP PUT.
	 *
	 * @param  url A URL addressing a modify service for users.
	 * @param  formParameters An XML file containing parameters for the new user.
	 * @return The response from the server.
	 */
	public String modifyUser (String url, Map<String, String> formParameters) throws IOException {
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		return handleRequest(url + "?" + form, Method.PUT, null);
		
	}

	/**
	 * Search for users via an HTTP GET.
	 *
	 * @param url A URL addressing a search service for users.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchUser(String url) throws IOException {

        return doSimpleGet(url);
	}

	/**
	 * Delete an existing user via an HTTP DELETE.
	 *
	 * @param  url A URL addressing a deletion service for users.
	 * @return The response from the server.
	 */
	public String deleteUser (String url) throws IOException {
		
		return handleRequest(url, Method.DELETE, null);
	}

	
	/****************** Groups **************************/
	
	/**
	 * Create a new group via an HTTP POST.
	 *
	 * @param url A URL addressing a creation service for groups.
	 * @param formParameters A Map of name-value pairs specifying a group.
	 * @return the result from either the server or an error message.
	 */
	public String createGroup (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		return handleRequest(url, Method.POST, rep);
	}
	
	/**
	 * Modify an existing group via an HTTP PUT.
	 *
	 * @param url A URL addressing a creation service for groups.
	 * @param formParameters A Map of name-value pairs specifying a group.
	 * @return the result from either the server or an error message.
	 */
	public String modifyGroup (String url, Map<String, String> formParameters) throws IOException {
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		return handleRequest(url + "?" + form, Method.PUT, null);
	}
	
	/**
	 * Modify an existing group via an HTTP PUT.
	 *
	 * @param  url A URL addressing a modify service for groups.
	 * @param  file An XML file containing parameters for the new group.
	 * @return The response from the server.
	 */
	// TODO: This fails with NetKernel as a server, possibly because it doesn't expect
	//       PUT bodies with this type of content type.
	public String modifyGroup (String url, File file) throws IOException {
		
		// Turn the file into a Representation.
		Representation rep = new FileRepresentation( file, MediaType.TEXT_XML, 3600 );
		return handleRequest(url, Method.PUT, rep);

	}

	/**
	 * Search for groups via an HTTP GET.
	 *
	 * @param url A URL addressing a search service for groups.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchGroup(String url) throws IOException {
	
        return doSimpleGet(url);
	}

	/**
	 * Delete an existing group via an HTTP DELETE.
	 *
	 * @param  url A URL addressing a deletion service for groups.
	 * @return The response from the server.
	 */
	public String deleteGroup (String url) throws IOException {
		
		return handleRequest(url, Method.DELETE, null);
	}
	
	
	/****************** Domains **************************/
	
	/**
	 * Create a new domain via an HTTP POST.
	 *
	 * @param  url A URL addressing a creation service for domains.
	 * @param formParameters A Map of name-value pairs specifying a domain.
	 * @return The response from the server.
	 */
	public String createDomain (String url, Map<String, String> formParameters) throws IOException {
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		return handleRequest(url, Method.POST, rep);
	}
	
	/**
	 * Modify an existing domain via an HTTP PUT.
	 *
	 * @param  url A URL addressing a modify service for domains.
	 * @param  formParameters An XML map containing parameters for the domain.
	 * @return The response from the server.
	 */
	public String modifyDomain (String url, Map<String, String> formParameters) throws IOException {
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		return handleRequest(url + "?" + form, Method.PUT, null);
	}

	/**
	 * Search for domains via an HTTP GET.
	 *
	 * @param url A URL addressing a search service for domains.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchDomain(String url) throws IOException {
        return doSimpleGet(url);
	}

	/**
	 * Delete an existing domain via an HTTP DELETE.
	 *
	 * @param  url A URL addressing a deletion service for domains.
	 * @return The response from the server (a String of XML or text).
	 */
	public String deleteDomain (String url) throws IOException {
		
		return handleRequest(url, Method.DELETE, null);
	}
	
	/****************** Utility Methods **************************/
	
	/**
	  * Create a RESTful request including a session cookie and send.
	  * This is a utility method to ease the creation of requests with
	  * a common cookie.
	  *
	  * @param url a String representation of a URL to request.
	  * @param method a Restlet Method (Method.GET, Method.POST, Method.PUT or Method.DELETE).
	  * @param rep a Representation to pass to the request.
	  */
	public String handleRequest (String url, Method method, Representation rep) {

		// Create a new request
		Request request = new Request(method, url);

		String cookie = CookieFactory.getCookie();
		// Add the session cookie and the representation.
		request.getCookies().add(new Cookie("NETKERNELSESSION", cookie));
        
		request.setEntity(rep);
		
		try {
			// Send the request.
			Response response = client.handle(request);
			// Return the contents of the response.
			return response.getEntity().getText();
		} catch (IOException e) {
		    e.printStackTrace();
			return e.toString() + ":" + e.getCause();
		}
	}

	
	/**
	 * URL encode a series of name-value pairs so they may be used in HTTP requests.
	 *
	 * @param  formParameters A Map of Strings representing name-value pairs.
	 * @return A URL-encoded "query string" representing the input.
	 */
	public String urlEncode(Map<String, String> formParameters) {
		
		String encodedValue = "";	
		try {
			if( formParameters != null ) {
				Set<String> parameters = formParameters.keySet();
				Iterator<String> it = parameters.iterator();
				StringBuffer buffer = new StringBuffer();

				for( int i = 0, paramCount = 0; it.hasNext(); i++ ) {
					String parameterName = it.next();
					String parameterValue = formParameters.get( parameterName );

					if( parameterValue != null ) {
						parameterValue = URLEncoder.encode( parameterValue, "UTF-8" );
						if( paramCount > 0 ) {
							buffer.append( "&" );
						} //if
						buffer.append( parameterName );
						buffer.append( "=" );
						buffer.append( parameterValue );
						++paramCount;
					} // if
				} // for
				encodedValue += buffer.toString();
			} // if

		} catch (UnsupportedEncodingException uee) {
			encodedValue = "";
		} // try/catch
		
		return encodedValue;
	}

     private String doSimpleGet(String url) throws IOException{
        Response response = client.get(url);
        String result = response.getEntity().getText();
        response = null ; System.gc();
        return result;
     }
} // class
