/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;

/** A common scheme and authority of a set of PURLs. This IRI should have a path of '/'. */
@iri("http://persistent.name/rdf/2010/purl#Origin")
public interface Origin extends DomainOrOrigin {
	@iri("http://persistent.name/rdf/2010/purl#part")
	Set<Domain> getPurlParts();
	@iri("http://persistent.name/rdf/2010/purl#part")
	void setPurlParts(Set<? extends Domain> purlParts);

}
