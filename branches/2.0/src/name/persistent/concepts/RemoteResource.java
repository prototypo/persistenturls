/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;

/** Indicates that this resource can be loaded using its definedBy target. */
@iri("http://persistent.name/rdf/2010/purl#RemoteResource")
public interface RemoteResource {
	/** A URL that will resolve to an RDF description of this resource. */
	@iri("http://persistent.name/rdf/2010/purl#definedBy")
	Set<RemoteGraph> getPurlDefinedBy();
	/** A URL that will resolve to an RDF description of this resource. */
	@iri("http://persistent.name/rdf/2010/purl#definedBy")
	void setPurlDefinedBy(Set<? extends RemoteGraph> purlDefinedBy);

	boolean load() throws Exception;

	boolean reload() throws Exception;

}
