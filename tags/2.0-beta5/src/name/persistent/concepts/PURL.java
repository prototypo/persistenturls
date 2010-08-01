/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.io.IOException;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.http.HttpResponse;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.iri;

/** A Persistent URL. */
@iri("http://persistent.name/rdf/2010/purl#PURL")
public interface PURL extends Resolvable {
	/** Simple redirection (302) */
	@iri("http://persistent.name/rdf/2010/purl#alternative")
	Set<Object> getPurlAlternatives();

	/** Simple redirection (302) */
	@iri("http://persistent.name/rdf/2010/purl#alternative")
	void setPurlAlternatives(Set<?> purlAlternatives);

	/** Copy of (203) */
	@iri("http://persistent.name/rdf/2010/purl#copyOf")
	Object getPurlCopyOf();

	/** Copy of (203) */
	@iri("http://persistent.name/rdf/2010/purl#copyOf")
	void setPurlCopyOf(Object purlCopyOf);

	/** See other (303) */
	@iri("http://persistent.name/rdf/2010/purl#describedBy")
	Set<Object> getPurlDescribedBy();

	/** See other (303) */
	@iri("http://persistent.name/rdf/2010/purl#describedBy")
	void setPurlDescribedBy(Set<?> purlDescribedBy);

	/** Parent partial or domain. */
	@iri("http://persistent.name/rdf/2010/purl#partOf")
	Partial getPurlPartOf();
	/** Parent partial or domain. */
	@iri("http://persistent.name/rdf/2010/purl#partOf")
	void setPurlPartOf(Partial purlPartOf);

	/** Temporary redirection (307) */
	@iri("http://persistent.name/rdf/2010/purl#redirectsTo")
	Object getPurlRedirectsTo();

	/** Temporary redirection (307) */
	@iri("http://persistent.name/rdf/2010/purl#redirectsTo")
	void setPurlRedirectsTo(Object purlRedirectsTo);

	/** Moved permanently (301) */
	@iri("http://persistent.name/rdf/2010/purl#renamedTo")
	Object getPurlRenamedTo();

	/** Moved permanently (301) */
	@iri("http://persistent.name/rdf/2010/purl#renamedTo")
	void setPurlRenamedTo(Object purlRenamedTo);

	void purlSetEntityHeaders(HttpResponse resp);

	Set<HttpResponse> purlValidate(Set<RDFObject> targets) throws IOException,
			DatatypeConfigurationException, RepositoryException;

}
