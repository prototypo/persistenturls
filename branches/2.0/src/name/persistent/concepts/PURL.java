/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;

@iri("http://persistent.name/rdf/2010/purl#PURL")
public interface PURL extends Resolvable {

	/** Simple redirection (302) */
	@iri("http://persistent.name/rdf/2010/purl#alternative")
	Set<Object> getPurlAlternatives();

	/** Simple redirection (302) */
	@iri("http://persistent.name/rdf/2010/purl#alternative")
	void setPurlAlternatives(Set<?> purlAlternatives);

	/** See other (303) */
	@iri("http://persistent.name/rdf/2010/purl#describedBy")
	Set<Object> getPurlDescribedBy();

	/** See other (303) */
	@iri("http://persistent.name/rdf/2010/purl#describedBy")
	void setPurlDescribedBy(Set<?> purlDescribedBy);

	/** Associates a PURL with a prefix domain. */
	@iri("http://persistent.name/rdf/2010/purl#partOf")
	Domain getPurlPartOf();
	/** Associates a PURL with a prefix domain. */
	@iri("http://persistent.name/rdf/2010/purl#partOf")
	void setPurlPartOf(Domain purlPartOf);

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

}
