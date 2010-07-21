/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import org.openrdf.repository.object.annotations.iri;

/** Resource is permanently gone (410) */
@iri("http://persistent.name/rdf/2010/purl#Tombstoned")
public interface Tombstoned extends Resolvable {
}
