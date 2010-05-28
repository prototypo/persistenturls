/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;

/** The protocol, hostname, and port of a server with PURL domains and list of services can resolve PURLs. This IRI should have a path of '/'. */
@iri("http://persistent.name/rdf/2010/purl#Server")
public interface Server {
	@iri("http://persistent.name/rdf/2010/purl#serves")
	Set<Domain> getPurlServes();
	@iri("http://persistent.name/rdf/2010/purl#serves")
	void setPurlServes(Set<? extends Domain> purlServes);

}
