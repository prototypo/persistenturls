/*
 * Copyright (c) 2010 Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** Domain that should be updated regularly from an authority. */
@iri("http://persistent.name/rdf/2010/purl#MirroredDomain")
public interface MirroredDomain extends Domain {

	void purlRefreshGraphs() throws Exception;

	void purlStallGraphs() throws Exception;
}
