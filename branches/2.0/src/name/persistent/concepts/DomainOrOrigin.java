/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;
import org.openrdf.repository.object.annotations.iri;

public interface DomainOrOrigin {
	/** A URL that will resolve to an RDF description all the domains or purls associated with this resource. */
	@iri("http://persistent.name/rdf/2010/purl#mirroredBy")
	Set<RemoteGraph> getPurlMirroredBy();
	/** A URL that will resolve to an RDF description all the domains or purls associated with this resource. */
	@iri("http://persistent.name/rdf/2010/purl#mirroredBy")
	void setPurlMirroredBy(Set<? extends RemoteGraph> purlMirroredBy);

}
