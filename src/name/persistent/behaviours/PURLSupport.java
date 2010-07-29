/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import static org.openrdf.http.object.util.Accepter.isCompatible;
import static org.openrdf.http.object.util.Accepter.parse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import name.persistent.concepts.PURL;
import name.persistent.concepts.Partial;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.http.object.client.HTTPObjectClient;
import org.openrdf.http.object.exceptions.InternalServerError;
import org.openrdf.http.object.util.Accepter;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.object.annotations.sparql;

/**
 * Resolves a PURL locally.
 * 
 * @author James Leigh
 */
public abstract class PURLSupport extends ResolvableSupport implements PURL {
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n";
	private static final String COPY_OF = "http://persistent.name/rdf/2010/purl#copyOf";
	private static final String RENAMED_TO = "http://persistent.name/rdf/2010/purl#renamedTo";
	private static final String ALTERNATIVE = "http://persistent.name/rdf/2010/purl#alternative";
	private static final String DESCRIBED_BY = "http://persistent.name/rdf/2010/purl#describedBy";
	private static final String REDIRECTS_TO = "http://persistent.name/rdf/2010/purl#redirectsTo";
	private static final String PROTOCOL = "1.1";
	private static final String VIA;
	private static String localhost;
	static {
		try {
			localhost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			localhost = "localhost";
		}
		VIA = PROTOCOL + " " + localhost;
	}

	private static class Location {
		private MimeType type;
		private MimeType language;
		private Collection<String> locations = new LinkedHashSet<String>();
		private HttpResponse response;
		private List<Object> key;

		public Location(int code, String type, String language,
				HttpResponse response) throws MimeTypeParseException {
			this.type = parse(type);
			this.language = parse(language);
			this.response = response;
			key = Arrays.asList(new Object[] { code, type, language });
		}

		public boolean addLocation(String location) {
			if (locations.add(location)) {
				if (response.getStatusLine().getStatusCode() < 300) {
					response.addHeader("Content-Location", location);
				} else {
					response.addHeader("Location", location);
				}
				return true;
			}
			return false;
		}

		public boolean removeLocation(String location) {
			for (Header hd : response.getHeaders("Location")) {
				if (hd.getValue().equals(location)) {
					response.removeHeader(hd);
				}
			}
			for (Header hd : response.getHeaders("Content-Location")) {
				if (hd.getValue().equals(location)) {
					response.removeHeader(hd);
				}
			}
			return locations.remove(location);
		}
	}

	private static class Link {
		private String href;
		private String title;
		private String type;
		private String media;
		private Collection<String> rels = new LinkedHashSet<String>();
		private Collection<String> languages = new LinkedHashSet<String>();
		private List<Object> key;

		public Link(String href, String title, String type, String media) {
			this.href = href;
			this.title = title;
			this.type = type;
			this.media = media;
			key = Arrays.asList(new Object[] { href, title, type, media });
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<").append(href).append(">");
			sb.append(";rel=\"");
			for (String rel : rels) {
				sb.append(rel).append(' ');
			}
			sb.setCharAt(sb.length() - 1, '"');
			for (String hreflang : languages) {
				sb.append(";hreflang=\"").append(hreflang).append("\"");
			}
			if (title != null) {
				sb.append(";title=\"").append(title).append("\"");
			}
			if (type != null) {
				sb.append(";type=\"").append(type).append("\"");
			}
			if (media != null) {
				sb.append(";media=\"").append(media).append("\"");
			}
			return sb.toString();
		}
	}

	@Override
	public HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via) throws Exception {
		TupleQueryResult result = findTargetURL();
		try {
			if (!result.hasNext())
				return new BasicHttpResponse(HTTP11, 404, "No Target");
			Map<Object, Location> map = new LinkedHashMap<Object, Location>();
			Collection<String> locations = new LinkedHashSet<String>();
			Map<Object, Link> links = new LinkedHashMap<Object, Link>();
			Matcher regex = null;
			while (result.hasNext()) {
				BindingSet set = result.next();
				if (regex == null) {
					Value pattern = set.getValue("pattern");
					regex = compile(pattern, source, qs);
				}
				String location = readValue(set, "target", regex);
				String rel = readValue(set, "rel", regex);
				String type = readValue(set, "type", regex);
				String lang = readValue(set, "lang", regex);
				String title = readValue(set, "title", regex);
				String media = readValue(set, "media", regex);
				locations.add(location);
				HttpResponse msg = response(set.getValue("pred").stringValue());
				if (msg != null) {
					int rc = msg.getStatusLine().getStatusCode();
					Location loc = new Location(rc, type, lang, msg);
					if (map.containsKey((Object) loc.key)) {
						loc = map.get((Object) loc.key);
					} else {
						map.put((Object) loc.key, loc);
					}
					if (loc.addLocation(location) && set.hasBinding("chain")) {
						Object purl = getObjectConnection().getObject(location);
						if (purl instanceof PURL) {
							HttpResponse inter = ((PURL) purl).resolvePURL(
									source, qs, accept, language, via);
							int ic = inter.getStatusLine().getStatusCode();
							if (ic == 301 || ic == 302 || ic == 307 || ic == rc) {
								loc.removeLocation(location);
								for (Header hd : inter
										.getHeaders("Content-Location")) {
									loc.addLocation(hd.getValue());
								}
								for (Header hd : inter.getHeaders("Location")) {
									loc.addLocation(hd.getValue());
								}
							}
						}
					}
				}
				Link link = new Link(location, title, type, media);
				if (links.containsKey((Object) link.key)) {
					link = links.get((Object) link.key);
				} else {
					links.put((Object) link.key, link);
				}
				if (lang != null) {
					link.languages.add(lang);
				}
				link.rels.add(rel);
			}
			StringBuilder sb = new StringBuilder();
			for (Link link : links.values()) {
				sb.append(link).append(", ");
			}
			String linkHeader = sb.substring(0, sb.length() - 2);
			HttpResponse resp;
			if (map.isEmpty()) {
				resp = new BasicHttpResponse(HTTP11, 300, "Multiple Choices");
				for (String location : locations) {
					resp.addHeader("Location", location);
				}
				resp.setHeader("Link", linkHeader);
			} else {
				resp = accept(map, accept, language, linkHeader);
			}
			if (resp == null)
				return new BasicHttpResponse(HTTP11, 406, "Not Acceptable");
			return prepareResponse(resp, linkHeader, via);
		} finally {
			result.close();
		}
	}

	public void purlSetEntityHeaders(HttpResponse resp) {
		Partial parent = getPurlPartOf();
		if (parent != null) {
			parent.purlSetEntityHeaders(resp);
		}
	}

	@sparql(PREFIX
			+ "SELECT REDUCED ?pattern ?pred ?rel ?target ?title ?type ?media ?lang ?chain\n"
			+ "WHERE { $this ?pred ?target . ?pred purl:rel ?rel \n"
			+ "OPTIONAL { $this purl:pattern ?pattern }\n"
			+ "OPTIONAL { ?target purl:title ?title }\n"
			+ "OPTIONAL { ?target purl:type ?type }\n"
			+ "OPTIONAL { ?target purl:media ?media }\n"
			+ "OPTIONAL { ?target purl:lang ?lang }\n"
			+ "OPTIONAL { ?target a ?unresolvable FILTER (?unresolvable = purl:Unresolvable) }\n"
			+ "OPTIONAL { ?target a ?chain FILTER (?chain = purl:PURL) }}\n"
			+ "ORDER BY ?unresolvable ?chain\n")
	protected abstract TupleQueryResult findTargetURL();

	protected Matcher compile(Value value, String source, String qs) {
		return null;
	}

	protected String apply(Matcher m, String template) {
		return template;
	}

	private HttpResponse accept(Map<Object, Location> map, String accept,
			String language, String linkHeader) throws MimeTypeParseException,
			IOException {
		Accepter typeAccepter = new Accepter(accept);
		Accepter langAccepter = new Accepter(language);
		for (MimeType mtype : typeAccepter.getAcceptable()) {
			for (MimeType ltype : langAccepter.getAcceptable()) {
				int code = 500;
				int lang = 0;
				HttpResponse resp = null;
				for (Location loc : map.values()) {
					int rc = loc.response.getStatusLine().getStatusCode();
					int rl = loc.language.toString().length();
					if ((rc < code || rc == code && rl > lang)
							&& isCompatible(mtype, loc.type)
							&& isCompatible(ltype, loc.language)) {
						code = rc;
						lang = rl;
						resp = loc.response;
					}
				}
				if (resp != null)
					return resp;
			}
		}
		return null;
	}

	private HttpResponse prepareResponse(HttpResponse resp, String linkHeader,
			Set<String> via) throws IOException {
		if (resp.containsHeader("Content-Location")) {
			String url = resp.getFirstHeader("Content-Location").getValue();
			HttpResponse bd = getFinalResponse(url, via);
			int code = bd.getStatusLine().getStatusCode();
			if (code == 200 || code == 203) {
				for (Header hd : bd.getAllHeaders()) {
					resp.removeHeaders(hd.getName());
				}
				resp.setHeader("Link", linkHeader);
				for (Header hd : bd.getAllHeaders()) {
					resp.addHeader(hd);
				}
				resp.setEntity(bd.getEntity());
				if (!resp.containsHeader("Last-Modified")
						&& resp.containsHeader("Date")) {
					String date = resp.getFirstHeader("Date").getValue();
					resp.setHeader("Last-Modified", date);
				}
			} else {
				HttpEntity entity = bd.getEntity();
				if (entity != null) {
					entity.consumeContent();
				}
				String msg = bd.getStatusLine().getReasonPhrase();
				return new BasicHttpResponse(HTTP11, 502, msg);
			}
		} else {
			resp.setHeader("Link", linkHeader);
			purlSetEntityHeaders(resp);
		}
		return resp;
	}

	private HttpResponse getFinalResponse(String url, Set<String> via)
			throws IOException {
		HTTPObjectClient client = HTTPObjectClient.getInstance();
		BasicHttpRequest req = new BasicHttpRequest("GET", url);
		StringBuilder sb = new StringBuilder();
		for (String v : via) {
			if (v.contains(VIA) && (v.endsWith(VIA) || v.contains(VIA + ",")))
				throw new InternalServerError("Request Loop Detected\n" + via
						+ "\n" + VIA);
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(v);
		}
		sb.append(VIA);
		req.setHeader("Via", sb.toString());
		HttpResponse bd = client.service(req);
		int code = bd.getStatusLine().getStatusCode();
		if (code >= 300 && code < 400) {
			HttpEntity entity = bd.getEntity();
			if (entity != null) {
				entity.consumeContent();
			}
			if (bd.containsHeader("Location")) {
				String loc = bd.getFirstHeader("Location").getValue();
				return getFinalResponse(loc, via);
			}
			String msg = bd.getStatusLine().getReasonPhrase();
			return new BasicHttpResponse(HTTP11, 502, msg);
		}
		return bd;
	}

	private String readValue(BindingSet set, String name, Matcher regex) {
		if (set.hasBinding(name)) {
			return apply(regex, set.getValue(name).stringValue());
		}
		return null;
	}

	private HttpResponse response(String rel) {
		if (COPY_OF.equals(rel))
			return new BasicHttpResponse(HTTP11, 203,
					"Non-Authoritative Information");
		if (RENAMED_TO.equals(rel))
			return new BasicHttpResponse(HTTP11, 301, "Moved Permanently");
		if (ALTERNATIVE.equals(rel))
			return new BasicHttpResponse(HTTP11, 302, "Found");
		if (DESCRIBED_BY.equals(rel))
			return new BasicHttpResponse(HTTP11, 303, "See Other");
		if (REDIRECTS_TO.equals(rel))
			return new BasicHttpResponse(HTTP11, 307, "Temporary Redirect");
		return null;
	}
}
