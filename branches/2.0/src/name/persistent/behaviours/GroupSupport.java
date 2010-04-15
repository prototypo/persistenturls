/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import name.persistent.concepts.Group;
import name.persistent.concepts.Party;
import name.persistent.concepts.User;

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
import org.openrdf.result.Result;

/**
 * Manages groups.
 * 
 * @author James Leigh
 */
public abstract class GroupSupport implements Group, RDFObject {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final String GROUP = "http://persistent.name/rdf/2010/purl#Group";

	public String getPurlId() {
		String uri = getResource().stringValue();
		int idx = uri.indexOf('/', uri.indexOf("/admin/") + "/admin/".length());
		return uri.substring(idx + 1);
	}

	/**
	 * Group name (name) Group maintainers (maintainers) Group members (members)
	 * Public comments (comments)
	 * 
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	@method("POST")
	@expect("201-created")
	@realm("/admin/realm")
	public void postUser(Map<String, String> parameters)
			throws RepositoryException, QueryEvaluationException {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() != null)
			throw new BadRequest("User Already Exists");
		getObjectConnection().addDesignations(this, GROUP);
		setPurlName(parameters.get("name"));
		setPurlNote(parameters.get("comments"));
		setPurlMaintainers(findParties(parameters.get("maintainers")));
		if (getPurlMaintainers().isEmpty())
			throw new BadRequest("Invalid Maintainers");
		setPurlMembers((Set) findParties(parameters.get("members")));
	}

	/**
	 * Group name (name) Group maintainers (maintainers) Group members (members)
	 * Public comments (comments)
	 * 
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	@method("PUT")
	@realm("/admin/realm")
	public void putUser(Map<String, String> parameters)
			throws RepositoryException, QueryEvaluationException {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() == null)
			throw new NotFound();
		setPurlName(parameters.get("name"));
		setPurlNote(parameters.get("comments"));
		setPurlMaintainers(findParties(parameters.get("maintainers")));
		if (getPurlMaintainers().isEmpty())
			throw new BadRequest("Invalid Maintainers");
		setPurlMembers((Set) findParties(parameters.get("members")));
	}

	@method("DELETE")
	@realm("/admin/realm")
	public void deleteUser() throws RepositoryException {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() == null)
			throw new NotFound();
		setPurlName(null);
		setPurlNote(null);
		getPurlMaintainers().clear();
		getPurlMembers().clear();
		getObjectConnection().removeDesignations(this, GROUP);
	}

	/**
	 * Group name (name) or Group maintainers (maintainers) or Group members
	 * (members) Search tombstoned (tombstone)
	 * 
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	@method("GET")
	@type("application/xml")
	public byte[] get(@parameter("name") String name,
			@parameter("maintainers") String maintainers,
			@parameter("members") String members) throws XMLStreamException,
			QueryEvaluationException, RepositoryException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = factory.createXMLStreamWriter(out);
		try {
			xml.writeStartDocument();
			String id = getPurlId();
			if (id == null || id.length() == 0) {
				Result<Group> results = checkAndFindGroups(name, maintainers,
						members);
				try {
					xml.writeStartElement("results");
					while (results.hasNext()) {
						results.next().writeTo(xml);
					}
					xml.writeEndElement();
				} finally {
					results.close();
				}
			} else {
				writeTo(xml);
			}
			xml.writeEndDocument();
		} catch (MalformedQueryException e) {
			throw new BadRequest("Invalid Parameters");
		} finally {
			xml.close();
		}
		return out.toByteArray();
	}

	public void writeTo(XMLStreamWriter xml) throws XMLStreamException {
		xml.writeStartElement("group");
		xml.writeAttribute("status", "1");
		write(xml, "id", getPurlId());
		write(xml, "name", getPurlName());
		write(xml, "comments", getPurlNote());
		xml.writeStartElement("maintainers");
		for (Party party : getPurlMaintainers()) {
			write(xml, "uid", party.getPurlId());
		}
		xml.writeEndElement();
		xml.writeStartElement("members");
		for (User user : getPurlMembers()) {
			write(xml, "uid", user.getPurlId());
		}
		xml.writeEndElement();
		xml.writeEndElement();
	}

	private Result<Group> checkAndFindGroups(String name, String maintainers,
			String members) throws RepositoryException,
			QueryEvaluationException, MalformedQueryException {
		if (name == null || name.length() == 0) {
			name = null;
		}
		if (maintainers == null || maintainers.length() == 0) {
			maintainers = null;
		}
		if (members == null || members.length() == 0) {
			members = null;
		}
		if (name == null && maintainers == null && members == null)
			throw new BadRequest("Missing Parameters");
		if (maintainers != null && maintainers.contains(">"))
			throw new BadRequest("Invalid Parameter");
		if (members != null && members.contains(">"))
			throw new BadRequest("Invalid Parameter");
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT ?group WHERE { ?group a purl:Group .\n");
		if (name != null) {
			sb.append("?group purl:name ?name .\n");
		}
		appendParties(sb, "maintainer", maintainers);
		appendParties(sb, "member", members);
		sb.append("}");
		String sparql = sb.toString();
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		ObjectQuery query = con.prepareObjectQuery(sparql);
		if (name != null) {
			query.setBinding("name", vf.createLiteral(name));
		}
		return query.evaluate(Group.class);
	}

	private void appendParties(StringBuilder sb, String part, String set) {
		if (set != null) {
			String prefix = getSchemeAuthority();
			String[] parties = set.split("\\s*");
			for (int i = 0; i < parties.length; i++) {
				if (i > 0) {
					sb.append("UNION ");
				}
				sb.append("{ ?group purl:").append(part).append(" <");
				sb.append(prefix).append("/admin/group/");
				sb.append(parties[i]).append("> }\n");
				sb.append("UNION { ?group purl:").append(part).append(" <");
				sb.append(prefix).append("/admin/user/");
				sb.append(parties[i]).append("> }\n");
			}
		}
	}

	private String getSchemeAuthority() {
		String source = getResource().stringValue();
		return source.substring(0, source.indexOf("/admin/"));
	}

	private void write(XMLStreamWriter xml, String tag, String value)
			throws XMLStreamException {
		if (value != null) {
			xml.writeStartElement(tag);
			xml.writeCharacters(value);
			xml.writeEndElement();
		}
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
			sb.append("/admin/group/").append(ids[i]).append(">");
			sb.append(" || ?party = <").append(prefix);
			sb.append("/admin/user/").append(ids[i]).append(">");
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
}
