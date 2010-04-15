/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.lang.Integer;
import java.lang.String;
import java.util.Set;
import org.openrdf.repository.object.annotations.iri;

/** A possible prefix to URLs. This URI must end with '/'. */
@iri("http://persistent.name/rdf/2010/purl#RequestedDomain")
public interface RequestedDomain {
	@iri("http://persistent.name/rdf/2010/purl#label")
	String getPurlLabel();
	@iri("http://persistent.name/rdf/2010/purl#label")
	void setPurlLabel(String purlLabel);

	/** Number of days between validating resolution of explicit PURL targets. */
	@iri("http://persistent.name/rdf/2010/purl#max-unresolved-days")
	Integer getPurlMaxUnresolvedDays();
	/** Number of days between validating resolution of explicit PURL targets. */
	@iri("http://persistent.name/rdf/2010/purl#max-unresolved-days")
	void setPurlMaxUnresolvedDays(Integer purlMaxUnresolvedDays);

	@iri("http://persistent.name/rdf/2010/purl#service")
	Set<Service> getPurlServices();
	@iri("http://persistent.name/rdf/2010/purl#service")
	void setPurlServices(Set<? extends Service> purlServices);

}
