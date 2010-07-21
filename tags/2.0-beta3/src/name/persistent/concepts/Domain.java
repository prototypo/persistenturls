/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.io.IOException;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.OpenRDFException;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.type;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.object.annotations.iri;

/** A partial PURL with its own access control list and a path endings with '/'. */
@iri("http://persistent.name/rdf/2010/purl#Domain")
public interface Domain extends Partial {
	/** Graph of all domains of this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#definedBy")
	Object getPurlDefinedBy();
	/** Graph of all domains of this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#definedBy")
	void setPurlDefinedBy(Object purlDefinedBy);

	/** Domains further up in this hierarchy. */
	@iri("http://persistent.name/rdf/2010/purl#domainOf")
	Set<Domain> getPurlDomainOf();
	/** Domains further up in this hierarchy. */
	@iri("http://persistent.name/rdf/2010/purl#domainOf")
	void setPurlDomainOf(Set<? extends Domain> purlDomainOf);

	/** Number of days between validating resolution of explicit PURL targets. */
	@iri("http://persistent.name/rdf/2010/purl#max-unresolved-days")
	Integer getPurlMaxUnresolvedDays();
	/** Number of days between validating resolution of explicit PURL targets. */
	@iri("http://persistent.name/rdf/2010/purl#max-unresolved-days")
	void setPurlMaxUnresolvedDays(Integer purlMaxUnresolvedDays);

	/** Graph of all the purls that are part of or belong to this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#mirroredBy")
	Object getPurlMirroredBy();
	/** Graph of all the purls that are part of or belong to this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#mirroredBy")
	void setPurlMirroredBy(Object purlMirroredBy);

	@iri("http://persistent.name/rdf/2010/purl#service")
	Set<Service> getPurlServices();
	@iri("http://persistent.name/rdf/2010/purl#service")
	void setPurlServices(Set<? extends Service> purlServices);

	/** Graph of all services for all domains of this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#servicedBy")
	Object getPurlServicedBy();
	/** Graph of all services for all domains of this domain (including itself). */
	@iri("http://persistent.name/rdf/2010/purl#servicedBy")
	void setPurlServicedBy(Object purlServicedBy);

	@type("text/plain")
	@operation("count-targets")
	int countTargets() throws QueryEvaluationException;

	boolean startResolving() throws QueryEvaluationException;

	boolean isResolving();

	boolean stopResolving() throws InterruptedException;

	void validatePURLs(XMLGregorianCalendar before, int min, int max,
			XMLGregorianCalendar today) throws OpenRDFException, IOException;

	void refreshGraphs() throws Exception;
}
