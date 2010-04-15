/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openrdf.http.object.annotations.expect;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.realm;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.exceptions.BadRequest;
import org.openrdf.http.object.exceptions.MethodNotAllowed;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.matches;
import org.openrdf.result.Result;

@matches("/admin/domain/*")
public abstract class DomainAdmin implements RDFObject {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";

	/**
	 * Domain name (name) Domain maintainers (maintainers) Domain writers
	 * (writers) Public (public)
	 * 
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 */
	@method("POST")
	@expect("201-created")
	@realm("/admin/realm")
	public void post(Map<String, String> parameters)
			throws RepositoryException, QueryEvaluationException {
		ObjectConnection con = getObjectConnection();
		Object target = con.getObject(getRealmURI());
		if (target instanceof Domain)
			throw new MethodNotAllowed("Entity Already Exists");
		Domain domain = con.addDesignation(target, Domain.class);
		domain.setPurlLabel(parameters.get("name"));
		domain.setPurlMaintainers(findParties(parameters.get("maintainers")));
		if (domain.getPurlMaintainers().isEmpty())
			throw new BadRequest("Invalid Maintainers");
		domain.setPurlCurators(findParties(parameters.get("writers")));
	}

	/**
	 * Domain name (name) Domain maintainers (maintainers) Domain writers
	 * (writers) Public (public)
	 */
	@method("PUT")
	@realm("/admin/realm")
	public void put(Map<String, String> parameters) throws RepositoryException,
			QueryEvaluationException {
		ObjectConnection con = getObjectConnection();
		Object target = con.getObject(getRealmURI());
		if (!(target instanceof Domain))
			throw new NotFound("Entity Does Not Exists");
		Domain domain = (Domain) target;
		domain.setPurlLabel(parameters.get("name"));
		domain.setPurlMaintainers(findParties(parameters.get("maintainers")));
		if (domain.getPurlMaintainers().isEmpty())
			throw new BadRequest("Invalid Maintainers");
		domain.setPurlCurators(findParties(parameters.get("writers")));
	}

	@method("DELETE")
	@realm("/admin/realm")
	public void delete() throws RepositoryException {
		ObjectConnection con = getObjectConnection();
		Object target = con.getObject(getRealmURI());
		if (!(target instanceof Domain))
			throw new NotFound("Entity Does Not Exists");
		Domain domain = (Domain) target;
		domain.setPurlLabel(null);
		domain.getPurlMaintainers().clear();
		domain.getPurlCurators().clear();
		con.removeDesignation(target, Domain.class);
	}

	/**
	 * Domain name (name) or Domain maintainer (maintainers) or Domain writer
	 * (writers) Search tombstoned (tombstone)
	 */
	@method("GET")
	@type("application/xml")
	public byte[] get(@parameter("name") String name,
			@parameter("maintainers") String maintainers,
			@parameter("writers") String writers) throws XMLStreamException,
			QueryEvaluationException, RepositoryException {
		ObjectConnection con = getObjectConnection();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = factory.createXMLStreamWriter(out);
		try {
			xml.writeStartDocument();
			if (name == null && maintainers == null && writers == null) {
				Object target = con.getObject(getRealmURI());
				if (!(target instanceof Domain))
					throw new NotFound("Entity Does Not Exists");
				write(xml, (Domain) target);
			} else {
				Result<Domain> results = checkAndFindRealms(name, maintainers,
						writers);
				try {
					xml.writeStartElement("results");
					while (results.hasNext()) {
						Domain target = results.next();
						write(xml, target);
					}
					xml.writeEndElement();
				} finally {
					results.close();
				}
			}
			xml.writeEndDocument();
		} catch (MalformedQueryException e) {
			throw new BadRequest("Invalid Parameters");
		} finally {
			xml.close();
		}
		return out.toByteArray();
	}

	private Result<Domain> checkAndFindRealms(String name, String maintainers,
			String writers) throws RepositoryException,
			QueryEvaluationException, MalformedQueryException {
		if (name == null || name.length() == 0) {
			name = null;
		}
		if (maintainers == null || maintainers.length() == 0) {
			maintainers = null;
		}
		if (writers == null || writers.length() == 0) {
			writers = null;
		}
		if (name == null && maintainers == null && writers == null)
			throw new BadRequest("Missing Parameters");
		if (maintainers != null && maintainers.contains(">"))
			throw new BadRequest("Invalid Parameter");
		if (writers != null && writers.contains(">"))
			throw new BadRequest("Invalid Parameter");
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT ?realm WHERE { ?realm a purl:Regoin .\n");
		if (name != null) {
			sb.append("?realm purl:label ?name .\n");
		}
		appendSet(sb, "maintainer", maintainers);
		appendSet(sb, "curator", writers);
		sb.append("}");
		String sparql = sb.toString();
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		ObjectQuery query = con.prepareObjectQuery(sparql);
		if (name != null) {
			query.setBinding("name", vf.createLiteral(name));
		}
		return query.evaluate(Domain.class);
	}

	private void appendSet(StringBuilder sb, String part, String set) {
		String prefix = getSchemeAuthority();
		if (set != null) {
			String[] parties = set.split("\\s*");
			for (int i = 0; i < parties.length; i++) {
				if (i > 0) {
					sb.append("UNION ");
				}
				sb.append("{ ?realm purl:").append(part).append(" <");
				sb.append(prefix).append("/admin/group/");
				sb.append(parties[i]).append("> }\n");
				sb.append("UNION { ?realm purl:").append(part).append(" <");
				sb.append(prefix).append("/admin/user/");
				sb.append(parties[i]).append("> }\n");
			}
		}
	}

	private void write(XMLStreamWriter xml, Domain domain)
			throws XMLStreamException {
		xml.writeStartElement("domain");
		xml.writeAttribute("status", "1");
		write(xml, "id", getId());
		write(xml, "name", domain.getPurlLabel());
		write(xml, "public", "true");
		xml.writeStartElement("maintainers");
		for (Party party : domain.getPurlMaintainers()) {
			write(xml, "uid", party.getPurlId());
		}
		xml.writeEndElement();
		xml.writeStartElement("writers");
		for (Party author : domain.getPurlCurators()) {
			write(xml, "uid", author.getPurlId());
		}
		xml.writeEndElement();
		xml.writeEndElement();
	}

	private String getId() {
		String uri = getResource().stringValue();
		int idx = uri.indexOf("/admin/domain/");
		int offset = "/admin/domain/".length();
		return uri.substring(idx + offset);
	}

	private void write(XMLStreamWriter xml, String tag, String value)
			throws XMLStreamException {
		if (value != null) {
			xml.writeStartElement(tag);
			xml.writeCharacters(value);
			xml.writeEndElement();
		}
	}

	private String getRealmURI() {
		String uri = getResource().stringValue();
		int idx = uri.indexOf("/admin/domain/");
		int offset = "/admin/domain/".length();
		return uri.substring(0, idx) + uri.substring(idx + offset);
	}

	private Set<? extends Party> findParties(String list)
			throws RepositoryException, QueryEvaluationException {
		if (list == null || list.length() == 0)
			return Collections.emptySet();
		String[] ids = list.split("\\s*");
		if (ids == null || ids.length == 0)
			return Collections.emptySet();
		String prefix = getSchemeAuthority();
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT ?party WHERE { ?party purl:name ?name\n");
		sb.append("FILTER (");
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				sb.append(" || ");
			}
			sb.append("?party = <").append(prefix);
			sb.append(ids[i]).append(">");
		}
		sb.append(") }");
		String sparql = sb.toString();
		ObjectConnection con = getObjectConnection();
		try {
			ObjectQuery query = con.prepareObjectQuery(sparql);
			return query.evaluate(Party.class).asSet();
		} catch (MalformedQueryException e) {
			throw new BadRequest("Invalid Parameters");
		}
	}

	private String getSchemeAuthority() {
		String source = getResource().stringValue();
		return source.substring(0, source.indexOf("/admin/domain/"));
	}
}
