/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;

public interface DomainOrGroupOrOriginOrPURL {
	/** Someone who may modify this resource. */
	@iri("http://persistent.name/rdf/2010/purl#maintainer")
	Set<Party> getPurlMaintainers();
	/** Someone who may modify this resource. */
	@iri("http://persistent.name/rdf/2010/purl#maintainer")
	void setPurlMaintainers(Set<? extends Party> purlMaintainers);

}
