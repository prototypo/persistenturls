/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.repository.object.annotations.iri;

/** Indicates that theses triples were loaded externally and need to be refreshed regularly. */
@iri("http://persistent.name/rdf/2010/purl#RemoteGraph")
public interface RemoteGraph {
	/** Only this resource and resources that start with the given URI can be loaded from this remote graph. */
	@iri("http://persistent.name/rdf/2010/purl#allowedOrigin")
	Set<Object> getPurlAllowedOrigins();
	/** Only this resource and resources that start with the given URI can be loaded from this remote graph. */
	@iri("http://persistent.name/rdf/2010/purl#allowedOrigin")
	void setPurlAllowedOrigins(Set<?> purlAllowedOrigins);

	/** Cache-Control HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#cache-control")
	String getPurlCacheControl();
	/** Cache-Control HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#cache-control")
	void setPurlCacheControl(String purlCacheControl);

	/** Content-Type HTTP header that was use when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#content-type")
	String getPurlContentType();
	/** Content-Type HTTP header that was use when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#content-type")
	void setPurlContentType(String purlContentType);

	/** ETag HTTP header that was use when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#etag")
	String getPurlEtag();
	/** ETag HTTP header that was use when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#etag")
	void setPurlEtag(String purlEtag);

	/** Last-Modified HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#last-modified")
	XMLGregorianCalendar getPurlLastModified();
	/** Last-Modified HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#last-modified")
	void setPurlLastModified(XMLGregorianCalendar purlLastModified);

	/** Date and time when this graph was last validated. */
	@iri("http://persistent.name/rdf/2010/purl#last-validated")
	XMLGregorianCalendar getPurlLastValidated();
	/** Date and time when this graph was last validated. */
	@iri("http://persistent.name/rdf/2010/purl#last-validated")
	void setPurlLastValidated(XMLGregorianCalendar purlLastValidated);

	/** Via HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#via")
	String getPurlVia();
	/** Via HTTP header that was used when this graph was loaded. */
	@iri("http://persistent.name/rdf/2010/purl#via")
	void setPurlVia(String purlVia);

	boolean load(String... origin) throws Exception;

	boolean validate(String origin) throws Exception;

	boolean reload(String origin) throws Exception;

	int getFreshness();

	boolean isFresh();

	void removeRemoteGraph() throws Exception;

}
