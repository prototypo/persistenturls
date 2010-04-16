/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import name.persistent.concepts.User;

import org.apache.commons.codec.digest.DigestUtils;
import org.openrdf.http.object.annotations.expect;
import org.openrdf.http.object.annotations.method;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.realm;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.exceptions.BadRequest;
import org.openrdf.http.object.exceptions.MethodNotAllowed;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.name;
import org.openrdf.repository.object.annotations.sparql;
import org.openrdf.result.Result;

/**
 * Manages users.
 * 
 * @author James Leigh
 */
public abstract class UserSupport implements User, RDFObject {
	private static final String USER = "http://persistent.name/rdf/2010/purl#User";

	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";

	public String getPurlId() {
		String uri = getResource().stringValue();
		int idx = uri.indexOf('/', uri.indexOf("/admin/") + "/admin/".length());
		return uri.substring(idx + 1);
	}

	public void setPasswd(String passwd) {
		if (passwd == null) {
			setPurlAlgorithm(null);
			setPurlEncoded(null);
		} else {
			setPurlAlgorithm("MD5");
			setPurlEncoded(DigestUtils.md5(passwd));
		}
	}

	/**
	 * Full name (name) Affiliation (affiliation) E-mail address (email)
	 * Password (passwd) Hint (hint) Justification (justification)
	 * 
	 * @throws RepositoryException
	 */
	@method("POST")
	@expect("201-created")
	public void postUser(Map<String, String> parameters)
			throws RepositoryException {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() != null)
			throw new BadRequest("User Already Exists");
		if (!parameters.containsKey("name")
				|| !parameters.containsKey("affiliation")
				|| !parameters.containsKey("email"))
			throw new BadRequest("Missing parameters");
		getObjectConnection().addDesignations(this, USER);
		setPurlName(parameters.get("name"));
		setPurlAffiliation(parameters.get("affiliation"));
		setPurlMbox(parameters.get("email"));
		setPasswd(parameters.get("passwd"));
		setPurlHint(parameters.get("hint"));
		setPurlJustification(parameters.get("justification"));
	}

	/**
	 * Full name (name) Affiliation (affiliation) E-mail address (email)
	 * Password (passwd) Hint (hint) Justification (justification)
	 */
	@method("PUT")
	@expect("204-no-content")
	@realm("/admin/realm")
	public void putUser(Map<String, String> parameters) {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() == null)
			throw new NotFound();
		if (!parameters.containsKey("name")
				|| !parameters.containsKey("affiliation")
				|| !parameters.containsKey("email"))
			throw new BadRequest("Missing parameters");
		setPurlName(parameters.get("name"));
		setPurlAffiliation(parameters.get("affiliation"));
		setPurlMbox(parameters.get("email"));
		setPasswd(parameters.get("passwd"));
		setPurlHint(parameters.get("hint"));
		setPurlJustification(parameters.get("justification"));
	}

	@method("DELETE")
	@expect("204-no-content")
	@realm("/admin/realm")
	public void deleteUser() throws RepositoryException {
		if (getResource().stringValue().endsWith("/"))
			throw new MethodNotAllowed();
		if (getPurlName() == null)
			throw new NotFound();
		setPurlName(null);
		setPurlAffiliation(null);
		setPurlMbox(null);
		setPasswd(null);
		setPurlHint(null);
		setPurlJustification(null);
		getObjectConnection().removeDesignations(this, USER);
	}

	@method("GET")
	@type("application/xml")
	public byte[] get(@parameter("fullname") String name,
			@parameter("affiliation") String affiliation,
			@parameter("email") String email) throws XMLStreamException,
			QueryEvaluationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = factory.createXMLStreamWriter(out);
		try {
			xml.writeStartDocument();
			String id = getPurlId();
			if (id == null || id.length() == 0) {
				Result<User> results = checkAndFindUsers(name, affiliation,
						email);
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
		} finally {
			xml.close();
		}
		return out.toByteArray();
	}

	public void writeTo(XMLStreamWriter xml) throws XMLStreamException {
		xml.writeStartElement("user");
		xml.writeAttribute("admin", "false");
		xml.writeAttribute("status", "1");
		write(xml, "id", getPurlId());
		write(xml, "name", getPurlName());
		write(xml, "affiliation", getPurlAffiliation());
		Object mbox = getPurlMbox();
		if (mbox != null) {
			write(xml, "email", mbox.toString());
		}
		xml.writeEndElement();
	}

	@sparql(PREFIX + "SELECT ?user\n" + "WHERE { ?user purl:name ?name;\n"
			+ "purl:affiliation ?affiliation; purl:mbox ?email }")
	protected abstract Result<User> findUsers(@name("name") String name,
			@name("affiliation") String affiliation, @name("email") String email);

	private Result<User> checkAndFindUsers(String name, String affiliation,
			String email) {
		if (name == null || name.length() == 0) {
			name = null;
		}
		if (affiliation == null || affiliation.length() == 0) {
			affiliation = null;
		}
		if (email == null || email.length() == 0) {
			email = null;
		}
		if (name == null && affiliation == null && email == null)
			throw new BadRequest("Missing Parameters");
		return findUsers(name, affiliation, email);
	}

	private void write(XMLStreamWriter xml, String tag, String value)
			throws XMLStreamException {
		if (value != null) {
			xml.writeStartElement(tag);
			xml.writeCharacters(value);
			xml.writeEndElement();
		}
	}
}
