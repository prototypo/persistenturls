/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** The priority and weight of a server capable of resolving PURLs. */
@iri("http://persistent.name/rdf/2010/purl#Service")
public interface Service {

	@iri("http://persistent.name/rdf/2010/purl#priority")
	Number getPurlPriority();
	@iri("http://persistent.name/rdf/2010/purl#priority")
	void setPurlPriority(Number purlPriority);

	@iri("http://persistent.name/rdf/2010/purl#server")
	Object getPurlServer();
	@iri("http://persistent.name/rdf/2010/purl#server")
	void setPurlServer(Object purlServer);

	@iri("http://persistent.name/rdf/2010/purl#weight")
	Number getPurlWeight();
	@iri("http://persistent.name/rdf/2010/purl#weight")
	void setPurlWeight(Number purlWeight);

}
