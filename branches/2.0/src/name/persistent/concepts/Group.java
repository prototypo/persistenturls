/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.util.Set;

import org.openrdf.repository.object.annotations.iri;
import org.openrdf.repository.object.annotations.matches;

/** A collection of users that share administrating privileges. */
@matches("/admin/group/*")
public interface Group extends Party, DomainOrGroupOrOriginOrPURL {
	@iri("http://persistent.name/rdf/2010/purl#member")
	Set<User> getPurlMembers();

	@iri("http://persistent.name/rdf/2010/purl#member")
	void setPurlMembers(Set<? extends User> purlMembers);

	@iri("http://persistent.name/rdf/2010/purl#note")
	String getPurlNote();

	@iri("http://persistent.name/rdf/2010/purl#note")
	void setPurlNote(String purlNote);

}
