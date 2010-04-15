/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** A PURL that also matches subpaths, if this path ends in a '/'. */
@iri("http://persistent.name/rdf/2010/purl#PartialPURL")
public interface PartialPURL extends PURL {
	/** Regular Expression of source URI, used to populate target URI */
	@iri("http://persistent.name/rdf/2010/purl#pattern")
	String getPurlPattern();
	/** Regular Expression of source URI, used to populate target URI */
	@iri("http://persistent.name/rdf/2010/purl#pattern")
	void setPurlPattern(String purlPattern);

}
