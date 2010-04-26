/*
 * Copyright (c) 2010 Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;
import org.openrdf.repository.object.annotations.iri;

public interface DomainOrOrigin {
	/** Someone who may modify this resource. */
	@iri("http://callimachusproject.org/rdf/2009/framework#maintainer")
	Set<Object> getCalliMaintainers();

}
