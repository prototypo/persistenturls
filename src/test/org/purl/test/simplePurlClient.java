package org.purl.test;

import java.net.*;
import java.io.*;
import java.util.*;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.util.*;
import org.restlet.resource.*;

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
	 * @param  
	 * @return 
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
	 * @param A URL addressing a modify service for PURLs.
	 * @param An XML file containing the new PURL parameters.
	 * @return 
	 */
	public String modifyPurl (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.put(url, rep).getEntity().getText();
	}

	/**
	 * Search for PURLs via an HTTP GET.
	 *
	 * @param A URL addressing a search service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String searchPurl(String url) throws IOException {
	
		Client client = new Client(Protocol.HTTP);
		return client.get(url).getEntity().getText();
	}

	/**
	 * Validate an existing PURL via an HTTP GET.
	 *
	 * @param A URL addressing a validation service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String validatePurl(String url) throws IOException {

		// TODO.
		return "Not implemented yet.";
	}

	/**
	 * Resolve an existing PURL via an HTTP GET.
	 *
	 * @param A URL addressing a validation service for PURLs.
	 * @return The response from the server (a String of XML or text).
	 */
	public String resolvePurl(String url) throws IOException {

		Client client = new Client(Protocol.HTTP);
		Map<String,Object> responseAttrs = client.get(url).getAttributes();
		Form headersForm = (Form) responseAttrs.get( "org.restlet.http.headers" );
		String locationHeader = headersForm.getFirst("Location").getValue();
		return locationHeader;
	}

	/**
	 * Delete an existing PURL via an HTTP DELETE.
	 *
	 * @param  
	 * @return 
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
	 * @param  
	 * @return 
	 */
	public String createPurls (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.post(url, rep).getEntity().getText();
	}
	
	/**
	 * Modify a batch of PURLs via an HTTP PUT.
	 *
	 * @param  url, a String representation of a URL to 
	 * @return 
	 */
	public String modifyPurls (String url, File file) throws IOException {
		
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
	public String modifyUser (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.put(url, rep).getEntity().getText();
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
	 * @param  
	 * @return 
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
	 * @param  A URL addressing a modify service for groups.
	 * @param  An XML file containing parameters for the new group.
	 * @return 
	 */
	public String modifyGroup (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
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
	public String modifyDomain (String url, File file) throws IOException {
		
		Client client = new Client(Protocol.HTTP);
		
		// Convert the form data to a RESTlet Representation.
		Representation rep = new FileRepresentation( file, MediaType.APPLICATION_XML, 3600 );
		
		// Request the resource and return its textual content.		
		return client.put(url, rep).getEntity().getText();
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
	
	
	/**
	 * Perform an HTTP GET and return the result.
	 *
	 * @param  A Uniform Resource Locator, presumed to use the HTTP scheme.
	 * @return The response from the URL's server (a String of XML or text).
	 */
	public String getURL(URL url)
		throws MalformedURLException, IOException {
	
		String result = "Not connected";
		try {
		    URLConnection urlConnection = url.openConnection();
		    urlConnection.connect();
		
			InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
			BufferedReader in = new BufferedReader(inputStream);
			result = "";  // Clear to help debugging.
			String inputLine = "";
			while ((inputLine = in.readLine()) != null) {
			            result += inputLine;
			}
			inputStream.close();
			in.close();
		
		} finally {
		}
	
		return result;
	}
	
	/**
	* Perform an HTTP request with the supplied method (GET, POST, PUT, DELETE)
	* on the supplied URL with the supplied requestHeaders, formParameters and
	* contents.
	* @return String the response contents
	* @param requestMethod the HTTP request method (one of GET, POST, PUT, DELETE)
	* @param url the URL to resolve
	* @param requestHeaders a Map of the request headernames and values to
	* be placed into the request
	* @param formParameters a Map of form parameters and values to be placed
	* into the request
	* @param contents the contents of the HTTP request
	* @throws ProtocolException reports problems performing an HTTP request
	* @throws IOException reports I/O sending and/or retrieving data over HTTP
	* @throws UnsupportedEncodingException reports a problem with URL encoding
*/
	public static String resolveURL ( String requestMethod,
					URL url,
	                Map requestHeaders,
	                Map formParameters,
	                String requestContents )
					throws ProtocolException, IOException, UnsupportedEncodingException {

		// Check the request method.
		if ( requestMethod != "GET" && requestMethod != "POST" && requestMethod != "PUT" && requestMethod != "DELETE" ) {
			throw new ProtocolException("Unsupported request method.  Only GET, POST, PUT or DELETE accepted.");
		}

		// Open a connection to the URL.
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// Set up URL connection to pass and retrieve information.
		con.setRequestMethod( requestMethod );
		con.setDoInput( true );
		if ( requestMethod == "GET" || requestMethod == "POST" || requestMethod == "PUT" ) {
			con.setDoOutput( true );
		}

		// add all the request headers
		if( requestHeaders != null ) {
			Set headers = requestHeaders.keySet();
			for( Iterator it = headers.iterator(); it.hasNext(); ) {
				String headerName = (String) it.next();
				String headerValue = (String) requestHeaders.get( headerName );
				con.setRequestProperty( headerName, headerValue );
			}  // for
		} // if

		// add url form parameters
		if ( requestMethod == "GET" || requestMethod == "POST" || requestMethod == "PUT" ) {
			DataOutputStream ostream = null;
			try {
				ostream = new DataOutputStream( con.getOutputStream() );
				if( formParameters != null ) {
					Set parameters = formParameters.keySet();
					Iterator it = parameters.iterator();
					StringBuffer buf = new StringBuffer();

					for( int i = 0, paramCount = 0; it.hasNext(); i++ ) {
						String parameterName = (String) it.next();
						String parameterValue = (String) formParameters.get( parameterName );

						if( parameterValue != null ) {
							parameterValue = URLEncoder.encode( parameterValue, "UTF-8" );
							if( paramCount > 0 ) {
								buf.append( "&" );
							} //if
							buf.append( parameterName );
							buf.append( "=" );
							buf.append( parameterValue );
							++paramCount;
						} // if
					} // for
					//System.out.println( "adding post parameters: " + buf.toString() );
					ostream.writeBytes( buf.toString() );
				} // if

				if( requestContents != null ) {
					ostream.writeBytes( requestContents );
				} // if

			} finally {
				if( ostream != null ) {
					ostream.flush();
					ostream.close();
				} // if
			} // try/finally
		} // if

		Object contents = con.getContent();
		InputStream is = (InputStream) contents;
		StringBuffer buf = new StringBuffer();
		int c;
		while( ( c = is.read() ) != -1 ) {
			buf.append( (char) c );
		} // while
		con.disconnect();
		return buf.toString();
	} // method resolveURL()

} // class
