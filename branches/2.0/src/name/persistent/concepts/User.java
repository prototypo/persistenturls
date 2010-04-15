/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** Approved credential for administrating PURLs. */
@iri("http://persistent.name/rdf/2010/purl#User")
public interface User extends RequestedUser {
}
