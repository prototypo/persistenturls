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
 * @author David Hyland-Wood.  david at http://zepheira.com
 * @version $Rev$
 */
public final class simplePurlClient {
    	
	/****************** Single PURLs **************************/
	
	/**
	 * Create a new PURL via an HTTP POST.
	 *
	 * @param  url, A URL addressing a creation service for PURLs.
	 * @return The response from the server.
	 */
	public String createPurl (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify an existing PURL via an HTTP PUT.
	 *
	 * @param url, A URL addressing a modify service for PURLs.
	 * @param An XML file containing the new PURL parameters.
	 * @return The response from the server.
	 */
	public String modifyPurl (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		
		// Request the resource and return its textual content.		
		return client.put(url + "?" + form, null).getEntity().getText();
		
	}

	/**
	 * Search for PURLs via an HTTP GET.
	 *
	 * @param url, A URL addressing a search service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchPurl(String url) throws IOException {
	
		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Validate an existing PURL via an HTTP GET.
	 *
	 * @param url, A URL addressing a validation service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String validatePurl(String url) throws IOException {

		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Resolve an existing PURL via an HTTP GET.
	 *
	 * @param url, A URL addressing a validation service for PURLs.
	 * @return The Location header from the server, if provided, or the status description if not.
	 */
	public String resolvePurl(String url) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
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
		return result;
	}

	/**
	 * Delete an existing PURL via an HTTP DELETE.
	 *
	 * @param  url, A URL addressing a validation service for PURLs.
	 * @return The response from the server.
	 */
	public String deletePurl (String url) throws IOException {

		Client client = new Client(Protocol.HTTP);

		// Request the resource and return its textual content.		
		return client.delete(url).getEntity().getText();
	}
	
	
	/****************** Batch PURLs **************************/
	
	/**
	 * Create a batch of PURLs via an HTTP POST.
	 *
	 * @param  url, a String representation of a URL to a batch creation service.
	 * @return The response from the server in XML.
	 */
	public String createPurls (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.TEXT_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify a batch of PURLs via an HTTP PUT.
	 *
	 * @param  url, a String representation of a URL to a batch modification service.
	 * @return The response from the server in XML.
	 */
	public String modifyPurls (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.put(url, rep).getEntity().getText();
	}

	/**
	 * Validate a batch of PURLs via an HTTP PUT.
	 *
	 * @param  url, a String representation of a URL to a batch validation service.
	 * @return The response from the server in XML.
	 */
	public String validatePurls (String url, File file) throws IOException {

		Client client = new Client(Protocol.HTTP);

		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );

		// Request the resource and return its textual content.		
		return client.put(url, rep).getEntity().getText();
	}
	
	/****************** Users **************************/
	
	/**
	 * Register a new user via an HTTP POST.
	 *
	 * @param  
	 * @return 
	 */
	public String registerUser (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify an existing user via an HTTP PUT.
	 *
	 * @param  A URL addressing a modify service for users.
	 * @param  An XML file containing parameters for the new user.
	 * @return 
	 */
	public String modifyUser (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		
		// Request the resource and return its textual content.	
		return client.put(url + "?" + form, null).getEntity().getText();
		
	}

	/**
	 * Search for users via an HTTP GET.
	 *
	 * @param A URL addressing a search service for users.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchUser(String url) throws IOException {
	
		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Delete an existing user via an HTTP DELETE.
	 *
	 * @param  
	 * @return 
	 */
	public String deleteUser (String url) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Request the resource and return its textual content.		
		return client.delete(url).getEntity().getText();
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
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify an existing group via an HTTP PUT.
	 *
	 * @param url A URL addressing a creation service for groups.
	 * @param formParameters A Map of name-value pairs specifying a group.
	 * @return the result from either the server or an error message.
	 */
	public String modifyGroup (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		
		// Request the resource and return its textual content.	
		return client.put(url + "?" + form, null).getEntity().getText();
	}
	
	/**
	 * Modify an existing group via an HTTP PUT.
	 *
	 * @param  A URL addressing a modify service for groups.
	 * @param  An XML file containing parameters for the new group.
	 * @return 
	 */
	// TODO: This fails with NetKernel as a server, possibly because it doesn't expect
	//       PUT bodies with this type of content type.
	public String modifyGroup (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Turn the file into a Representation.
		Representation rep = new FileRepresentation( file, MediaType.TEXT_XML, 3600 );
			
		// Request the resource and return its textual content.
		return client.put(url, rep).getEntity().getText();

	}

	/**
	 * Search for groups via an HTTP GET.
	 *
	 * @param A URL addressing a search service for groups.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchGroup(String url) throws IOException {
	
		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Delete an existing group via an HTTP DELETE.
	 *
	 * @param  
	 * @return 
	 */
	public String deleteGroup (String url) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Request the resource and return its textual content.		
		return client.delete(url).getEntity().getText();
	}
	
	
	/****************** Domains **************************/
	
	/**
	 * Create a new domain via an HTTP POST.
	 *
	 * @param  
	 * @return 
	 */
	public String createDomain (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		String form = urlEncode(formParameters);
		Representation rep = new StringRepresentation( form, MediaType.APPLICATION_WWW_FORM );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify an existing domain via an HTTP PUT.
	 *
	 * @param  A URL addressing a modify service for domains.
	 * @param  An XML file containing parameters for the new domain.
	 * @return 
	 */
	public String modifyDomain (String url, Map<String, String> formParameters) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
				
		// Encode the form so it will pass on a URL.
		String form = urlEncode(formParameters);
		
		// Request the resource and return its textual content.	
		return client.put(url + "?" + form, null).getEntity().getText();
	}

	/**
	 * Search for domains via an HTTP GET.
	 *
	 * @param A URL addressing a search service for domains.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchDomain(String url) throws IOException {
	
		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Delete an existing domain via an HTTP DELETE.
	 *
	 * @param  
	 * @return 
	 */
	public String deleteDomain (String url) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Request the resource and return its textual content.		
		return client.delete(url).getEntity().getText();
	}
	
	/****************** Utility Methods **************************/
	
	/**
	 * URL encode a series of name-value pairs so they may be used in HTTP requests.
	 *
	 * @param  A Map of Strings representing name-value pairs.
	 * @return A URL-encoded "query string" representing the input.
	 */
	public String urlEncode(Map<String, String> formParameters) {
		
		String encodedValue = "";	
		try {
			if( formParameters != null ) {
				Set parameters = formParameters.keySet();
				Iterator it = parameters.iterator();
				StringBuffer buffer = new StringBuffer();

				for( int i = 0, paramCount = 0; it.hasNext(); i++ ) {
					String parameterName = (String) it.next();
					String parameterValue = (String) formParameters.get( parameterName );

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
	

} // class
