/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import info.aduna.net.ParsedURI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
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
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.RDFObject;
import org.openrdf.repository.object.annotations.matches;
import org.openrdf.result.Result;

@matches( { "/admin/purl/*", "/admin/purls", "/admin/targeturl/*" })
public abstract class PURLAdmin implements RDFObject {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";

	@method("POST")
	@realm("/admin/realm")
	public void postPURL(@type("application/xml") XMLEventReader xml)
			throws Exception {
		Map<String, String[]> map = new HashMap<String, String[]>();
		String uri = null;
		while (xml.hasNext()) {
			XMLEvent event = xml.nextEvent();
			switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement start = event.asStartElement();
				String name = start.getName().getLocalPart();
				if ("purl".equals(name)) {
					map.clear();
					Attribute pid = start.getAttributeByName(new QName("id"));
					uri = getSchemeAuthority() + pid.getValue();
					Attribute type = start
							.getAttributeByName(new QName("type"));
					map.put("type", new String[] { type.getValue() });
				} else if ("uid".equals(name)) {
					String uid = xml.getElementText();
					if (map.containsKey("maintainers")) {
						String value = map.get("maintainers") + "\n" + uid;
						map.put("maintainers", new String[] { value });
					} else {
						map.put("maintainers", new String[] { uid });
					}
				} else if ("target".equals(name)) {
					Attribute url = start.getAttributeByName(new QName("url"));
					map.put("target", new String[] { url.getValue() });
				} else if ("seealso".equals(name)) {
					Attribute url = start.getAttributeByName(new QName("url"));
					if (map.containsKey("seealso")) {
						String[] ar = map.get("seealso");
						String[] value = new String[ar.length + 1];
						System.arraycopy(ar, 0, value, 0, ar.length);
						value[value.length - 1] = url.getValue();
						map.put("seealso", value);
					} else {
						map.put("seealso", new String[] { url.getValue() });
					}
				} else if ("basepurl".equals(name)) {
					Attribute path = start
							.getAttributeByName(new QName("path"));
					map.put("target", new String[] { getSchemeAuthority()
							+ path.getValue() });
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				if ("purl"
						.equals(event.asEndElement().getName().getLocalPart())) {
					assert uri != null && map.containsKey("type");
					try {
						post(uri, map);
					} catch (MethodNotAllowed e) {
						String msg = e.getMessage();
						if (event.getLocation() != null) {
							msg += " on line "
									+ event.getLocation().getLineNumber();
						}
						throw new BadRequest(msg);
					}
				}
				break;
			}
		}
	}

	/**
	 * Target URL (target) User/Group IDs (maintainers) Type (type) See Also
	 * URLs (seealso)
	 * 
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	@method("POST")
	@expect("201-created")
	@realm("/admin/realm")
	@type("application/xml")
	public byte[] postPURL(
			@type("application/x-www-form-urlencoded") Map<String, String[]> parameters)
			throws Exception {
		return post(getPURL(), parameters);
	}

	@method("PUT")
	@realm("/admin/realm")
	@type("application/xml")
	public byte[] putPURL(@parameter("*") Map<String, String[]> parameters,
			@type("text/html") byte[] body) throws Exception {
		return putPURL(parameters);
	}

	/**
	 * Target URL (target) User/Group IDs (maintainers) Type (type) See Also
	 * URLs (seealso)
	 * 
	 * @throws QueryEvaluationException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	@method("PUT")
	@realm("/admin/realm")
	@type("application/xml")
	public byte[] putPURL(Map<String, String[]> parameters)
			throws Exception {
		ObjectConnection con = getObjectConnection();
		String uri = getPURL();
		Object url = con.getObject(uri);
		if (!(url instanceof PURL))
			throw new NotFound("Entity Does Not Exists");
		String[] type = parameters.get("type");
		String[] target = parameters.get("target");
		String[] seealso = parameters.get("seealso");
		if (type == null || type.length != 1)
			throw new BadRequest("Invalid type Parameter");
		String[] maintainers = parameters.get("maintainers");
		if ("partial".equals(type[0]) && !uri.endsWith("/")) {
			if (target == null || target.length != 1)
				throw new BadRequest("Invalid target Parameter");
			type = new String[] { "302" };
			put(url, type, target, seealso, maintainers);
			type = new String[] { "partial" };
			target = new String[] { target[0] + "/" };
			return put(con.getObject(uri + "/"), type, target, seealso, maintainers);
		} else {
			return put(url, type, target, seealso, maintainers);
		}
	}

	@method("DELETE")
	@realm("/admin/realm")
	@type("application/xml")
	public byte[] deletePURL() throws Exception {
		ObjectConnection con = getObjectConnection();
		Object url = con.getObject(getPURL());
		if (!(url instanceof PURL))
			throw new NotFound("Entity Does Not Exists");
		TombstonedPURL purl = con.addDesignation(url, TombstonedPURL.class);
		return write(purl);
	}

	/**
	 * Target URL (target) or See Also URLs (see also) or User/Group IDs
	 * (maintainers) or Explicit User/Group ID (explicitmaintainers) Search
	 * tombstoned (tombstone)
	 * 
	 * @throws IOException
	 */
	@method("GET")
	@type("application/xml")
	public byte[] getPURL(@parameter("p_id") String p_id,
			@parameter("target") String uri,
			@parameter("maintainers") String maintainers,
			@parameter("tombstone") Boolean tombstone) throws Exception {
		ObjectConnection con = getObjectConnection();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = factory.createXMLStreamWriter(out);
		try {
			xml.writeStartDocument();
			if (p_id == null && uri == null && maintainers == null && tombstone == null) {
				Object target = con.getObject(getPURL());
				if (!(target instanceof PURL))
					throw new NotFound("Entity Does Not Exists");
				write(xml, (PURL) target, true);
			} else if (p_id != null & p_id.length() > 0) {
				Object target = con.getObject(getSchemeAuthority() + p_id);
				if (!(target instanceof PURL))
					throw new NotFound("PURL Does Not Exists");
				write(xml, (PURL) target, true);
			} else {
				Result<PURL> results = checkAndFindPURLs(uri, maintainers,
						tombstone);
				try {
					xml.writeStartElement("results");
					while (results.hasNext()) {
						write(xml, results.next(), false);
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

	private byte[] post(String uri, Map<String, String[]> parameters)
			throws Exception {
		ObjectConnection con = getObjectConnection();
		Object url = con.getObject(uri);
		if (url instanceof PURL)
			throw new MethodNotAllowed("Entity Already Exists");
		String[] type = parameters.get("type");
		String[] target = parameters.get("target");
		String[] seealso = parameters.get("seealso");
		String[] maintainers = parameters.get("maintainers");
		if (type == null || type.length != 1)
			throw new BadRequest("Invalid type Parameter");
		if ("partial".equals(type[0]) && !uri.endsWith("/")) {
			if (target == null || target.length != 1)
				throw new BadRequest("Invalid target Parameter");
			type = new String[] { "302" };
			post(url, type, target, seealso, maintainers);
			type = new String[] { "partial" };
			target = new String[] { target[0] + "/" };
			return post(con.getObject(uri + "/"), type, target, seealso,
					maintainers);
		} else {
			return post(url, type, target, seealso, maintainers);
		}
	}

	private byte[] post(Object url, String[] type, String[] target,
			String[] seealso, String[] maintainers) throws Exception {
		PURL purl = designate(url, type, target);
		assignTarget(purl, type, target, seealso);
		if (maintainers != null && maintainers.length > 0
				&& maintainers[0].length() > 0) {
			purl.setPurlMaintainers(findParties(maintainers));
		}
		return write(purl);
	}

	private byte[] write(PURL purl) throws FactoryConfigurationError,
			XMLStreamException, Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xml = factory.createXMLStreamWriter(out);
		try {
			xml.writeStartDocument();
			write(xml, purl, true);
			xml.writeEndDocument();
		} catch (MalformedQueryException e) {
			throw new BadRequest("Invalid Parameters");
		} finally {
			xml.close();
		}
		return out.toByteArray();
	}

	private byte[] put(Object url, String[] type, String[] target,
			String[] seealso, String[] maintainers) throws Exception {
		PURL purl = designate(url, type, target);
		purl.setPurlRenamedTo(null);
		purl.setPurlAlternatives(null);
		purl.setPurlDescribedBy(null);
		purl.setPurlRedirectsTo(null);
		assignTarget(purl, type, target, seealso);
		if (maintainers != null && maintainers.length > 0
				&& maintainers[0].length() > 0) {
			purl.setPurlMaintainers(findParties(maintainers));
		}
		return write(purl);
	}

	private PURL designate(Object url, String[] types, String[] target)
			throws RepositoryException {
		if (types == null || types.length != 1)
			throw new BadRequest("Invalid type Parameter");
		String type = types[0];
		ObjectConnection con = getObjectConnection();
		con.removeDesignation(url, TombstonedPURL.class);
		con.removeDesignation(url, DisabledPURL.class);
		con.removeDesignation(url, PartialPURL.class);
		con.removeDesignation(url, ZonedPURL.class);
		if ("clone".equals(type)) {
			if (target == null || target.length == 0)
				throw new BadRequest("Missing target");
			Object tpurl = con.getObject(target[0]);
			if (!(tpurl instanceof PURL))
				throw new BadRequest("Target is Not a Local PURL");
			PURL source = (PURL) tpurl;
			if (tpurl instanceof DisabledPURL) {
				url = con.addDesignation(url, DisabledPURL.class);
			}
			if (tpurl instanceof TombstonedPURL) {
				url = con.addDesignation(url, TombstonedPURL.class);
			}
			if (tpurl instanceof PartialPURL) {
				url = con.addDesignation(url, PartialPURL.class);
			}
			if (tpurl instanceof ZonedPURL) {
				url = con.addDesignation(url, ZonedPURL.class);
			}
			if (!(url instanceof PURL)) {
				url = con.addDesignation(url, PURL.class);
			}
			PURL purl = (PURL) url;
			purl.setPurlRenamedTo(source.getPurlRenamedTo());
			purl.setPurlAlternatives(source.getPurlAlternatives());
			purl.setPurlDescribedBy(source.getPurlDescribedBy());
			purl.setPurlRedirectsTo(source.getPurlRedirectsTo());
			purl.setPurlMaintainers(source.getPurlMaintainers());
			return purl;
		} else if ("404".equals(type)) {
			return con.addDesignation(url, DisabledPURL.class);
		} else if ("410".equals(type)) {
			return con.addDesignation(url, TombstonedPURL.class);
		} else if ("partial".equals(type)) {
			return con.addDesignation(url, PartialPURL.class);
		} else {
			return con.addDesignation(url, PURL.class);
		}
	}

	private void assignTarget(PURL purl, String[] types, String[] target,
			String[] seealso) {
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		if (types == null || types.length != 1)
			throw new BadRequest("Invalid type Parameter");
		String type = types[0];
		if ("303".equals(type)) {
			if (seealso == null || seealso.length == 0)
				throw new BadRequest("Missing seealso Parameter");
			for (String also : seealso) {
				purl.getPurlDescribedBy().add(of.createObject(also));
			}
		} else if (!"404".equals(type) && !"410".equals(type)
				&& !"clone".equals(type)) {
			if (target == null || target.length != 1)
				throw new BadRequest("Invalid target Parameter");
			if ("301".equals(type)) {
				purl.setPurlRenamedTo(of.createObject(target[0]));
			} else if ("302".equals(type) || "chain".equals(type)) {
				purl.getPurlAlternatives().add(of.createObject(target[0]));
			} else if ("partial".equals(type)) {
				String pattern = purl.toString();
				pattern = pattern.replaceAll("[^/:@]+/", "[^/]+/") + "(.*)";
				((PartialPURL) purl).setPurlPattern(pattern);
				purl.getPurlAlternatives().add(
						of.createObject(target[0] + "$1"));
			} else if ("307".equals(type)) {
				purl.setPurlRedirectsTo(of.createObject(target[0]));
			} else {
				throw new BadRequest("Invalid PURL Type");
			}
		}
	}

	private Result<PURL> checkAndFindPURLs(String uri, String maintainers,
			Boolean tombstone) throws RepositoryException,
			QueryEvaluationException, MalformedQueryException {
		if (uri == null || uri.length() == 0) {
			uri = null;
		}
		if (maintainers == null || maintainers.length() == 0) {
			maintainers = null;
		}
		if (uri == null && maintainers == null)
			throw new BadRequest("Missing Parameters");
		if (uri != null && uri.contains(">"))
			throw new BadRequest("Invalid Parameter");
		if (maintainers != null && maintainers.contains(">"))
			throw new BadRequest("Invalid Parameter");
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT ?purl WHERE {\n");
		sb.append("{ ?purl a purl:PURL }\n");
		sb.append("UNION { ?purl a purl:PartialPURL }\n");
		if (tombstone != null && tombstone) {
			sb.append("UNION { ?purl a purl:ZonedPURL }\n");
		} else {
			sb.append("OPTIONAL { ?purl a ?type\n");
			sb.append("FILTER (?type = purl:TombstonePURL)}\n");
			sb.append("FILTER (!bound(?type))");
		}
		if (uri != null) {
			sb.append("?purl ?rel ?target .\n");
		}
		appendSet(sb, "maintainer", maintainers);
		sb.append("}");
		String sparql = sb.toString();
		ObjectConnection con = getObjectConnection();
		ValueFactory vf = con.getValueFactory();
		ObjectQuery query = con.prepareObjectQuery(sparql);
		if (uri != null) {
			query.setBinding("target", vf.createURI(uri));
		}
		return query.evaluate(PURL.class);
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

	private void write(XMLStreamWriter xml, PURL purl, boolean validate)
			throws Exception {
		xml.writeStartElement("purl");
		xml.writeAttribute("status", "1");
		HttpResponse resp = purl.resolvePURL(purl.toString(), null, "*/*", "*",
				20);
		int code = resp.getStatusLine().getStatusCode();
		Header location = resp.getFirstHeader("Location");
		if (validate && location != null) {
			HttpURLConnection con = (HttpURLConnection) new URL(location
					.getValue()).openConnection();
			con.setInstanceFollowRedirects(false);
			con.setRequestMethod("HEAD");
			int responseCode = con.getResponseCode();
			if (responseCode >= 400) {
				xml.writeAttribute("validation", "failure");
				write(xml, "message", con.getResponseMessage());
			} else if (responseCode != 200) {
				xml.writeAttribute("validation", "success");
				write(xml, "message", con.getResponseMessage());
			} else {
				xml.writeAttribute("validation", "success");
			}
		}
		write(xml, "id", new ParsedURI(purl.toString()).getPath());
		if (purl instanceof PartialPURL) {
			write(xml, "type", "partial");
		} else {
			write(xml, "type", Integer.toString(code));
		}
		xml.writeStartElement("maintainers");
		for (Party party : purl.getPurlMaintainers()) {
			write(xml, "uid", party.getPurlId());
		}
		xml.writeEndElement();
		xml.writeStartElement("target");
		if (location != null) {
			write(xml, "url", location.getValue());
		}
		xml.writeEndElement();
		xml.writeEndElement();
	}

	private void write(XMLStreamWriter xml, String tag, String value)
			throws XMLStreamException {
		if (value != null) {
			xml.writeStartElement(tag);
			xml.writeCharacters(value);
			xml.writeEndElement();
		}
	}

	private Set<? extends Party> findParties(String... lists)
			throws RepositoryException, QueryEvaluationException {
		if (lists == null || lists.length == 0)
			return Collections.emptySet();
		String prefix = getSchemeAuthority();
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT ?party WHERE { ?party purl:name ?name\n");
		sb.append("FILTER (");
		boolean first = true;
		for (String list : lists) {
			if (list == null || list.length() == 0)
				return Collections.emptySet();
			String[] ids = list.split("\\s*");
			if (ids == null || ids.length == 0)
				return Collections.emptySet();
			for (int i = 0; i < ids.length; i++) {
				if (first) {
					first = false;
				} else {
					sb.append(" || ");
				}
				sb.append("?party = <").append(prefix);
				sb.append(ids[i]).append(">");
			}
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

	private String getId() {
		String uri = getResource().stringValue();
		int pidx = uri.indexOf("/admin/");
		int offset = "/admin/".length();
		int idx = uri.indexOf('/', pidx + offset);
		return uri.substring(idx);
	}

	private String getPURL() {
		return getSchemeAuthority() + getId();
	}

	private String getSchemeAuthority() {
		String source = getResource().stringValue();
		return source.substring(0, source.indexOf("/admin/"));
	}
}
