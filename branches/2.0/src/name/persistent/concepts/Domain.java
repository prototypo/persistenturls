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

import org.apache.http.HttpResponse;
import org.openrdf.OpenRDFException;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.type;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.object.annotations.iri;

/** A prefix to a set of PURLs (endings with '/') that are administrated together. */
@iri("http://persistent.name/rdf/2010/purl#Domain")
public interface Domain extends PURL {
	@iri("http://persistent.name/rdf/2010/purl#domainOf")
	Domain getPurlDomainOf();
	@iri("http://persistent.name/rdf/2010/purl#domainOf")
	void setPurlDomainOf(Domain domainOf);

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

	@type("text/plain")
	@operation("count-targets")
	int countTargets() throws QueryEvaluationException;

	HttpResponse resolveRemotePURL(String source, String qs, String accept,
			String language, Set<String> via) throws Exception;

	boolean startResolving() throws QueryEvaluationException;

	boolean isResolving();

	boolean stopResolving() throws InterruptedException;

	void validatePURLs(XMLGregorianCalendar before, int min, int max,
			XMLGregorianCalendar today) throws OpenRDFException, IOException;
}
