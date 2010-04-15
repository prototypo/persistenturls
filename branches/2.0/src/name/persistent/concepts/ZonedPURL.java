/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** A PURL that matches authorities with a DNS domain of this PURL's DNS host name. */
@iri("http://persistent.name/rdf/2010/purl#ZonedPURL")
public interface ZonedPURL extends PURL {
}
