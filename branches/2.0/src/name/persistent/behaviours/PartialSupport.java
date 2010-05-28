/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import name.persistent.concepts.Disabled;
import name.persistent.concepts.Domain;
import name.persistent.concepts.PURL;
import name.persistent.concepts.RemoteGraph;
import name.persistent.concepts.RemoteResource;
import name.persistent.concepts.Resolvable;
import name.persistent.concepts.Zoned;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.BadGateway;
import org.openrdf.http.object.exceptions.GatewayTimeout;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.RDFObject;

/**
 * Finds a PURL (possibly remote) for a request.
 * 
 * @author James Leigh
 */
public abstract class PartialSupport extends MirrorSupport implements
		RDFObject, Resolvable {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

	public HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via) throws Exception {
		return resolvePURL(source, qs, accept, language, via, 4);
	}

	private HttpResponse resolvePURL(String source, String qs, String accept,
			String lang, Set<String> via, int maxload) throws Exception {
		if (maxload < 0)
			throw new BadGateway("Unknown PURL");
		ObjectConnection con = getObjectConnection();
		String sparql = createSPARQL(source);
		ObjectQuery query = con.prepareObjectQuery(sparql);
		List<?> result = query.evaluate().asList();
		if (result.isEmpty()) {
			loadOrigin();
			return resolvePURL(source, qs, accept, lang, via, maxload - 1);
		} else {
			Object purl = result.get(0);
			if (purl instanceof Domain) {
				Domain domain = (Domain) purl;
				if (domain instanceof RemoteResource) {
					if (!((RemoteResource) domain).reload()) {
						loadOrigin();
						return resolvePURL(source, qs, accept, lang, via,
								maxload - 1);
					}
				}
				HttpResponse resp;
				resp = domain.resolvePURL(source, qs, accept, lang, via);
				int code = resp.getStatusLine().getStatusCode();
				if (domain instanceof Disabled || code != 404) {
					if (!resolvesTo(source, domain)) {
						consumeContent(resp);
						throw new NotFound("Unknown Persistent URL");
					}
					return resp;
				}
				consumeContent(resp);
				return domain.resolveRemotePURL(source, qs, accept, lang, via);
			} else if (purl instanceof PURL) {
				if (!resolvesTo(source, purl))
					throw new NotFound("Unknown Persistent URL");
				return ((PURL) purl).resolvePURL(source, qs, accept, lang, via);
			} else if (purl instanceof RemoteResource) {
				if (!((RemoteResource) purl).load()) {
					loadOrigin();
				}
				return resolvePURL(source, qs, accept, lang, via, maxload - 1);
			} else {
				throw new AssertionError("Invalid resource type: " + purl);
			}
		}
	}

	private boolean resolvesTo(String source, Object purl) {
		return source.toString().startsWith(purl.toString()) || purl instanceof Zoned;
	}

	private void loadOrigin() throws Exception {
		ParsedURI parsed = new ParsedURI(getResource().stringValue());
		String scheme = parsed.getScheme();
		String auth = parsed.getAuthority();
		assert auth != null;
		String originURI = new ParsedURI(scheme, auth, "/", null, null).toString();
		String zo = getZonedOrigin(scheme, auth);
		ObjectConnection con = getObjectConnection();
		ObjectFactory of = con.getObjectFactory();
		Exception gateway = null;
		for (InetSocketAddress addr : getOriginServices(true)) {
			String host = addr.getHostName();
			if ("http".equalsIgnoreCase(scheme) && addr.getPort() != 80) {
				host += ":" + addr.getPort();
			} else if ("https".equalsIgnoreCase(scheme)
					&& addr.getPort() != 443) {
				host += ":" + addr.getPort();
			}
			String uri = new ParsedURI(scheme, host, "/", null, null)
					.toString();
			try {
				RemoteGraph server = of.createObject(uri, RemoteGraph.class);
				if (server.load(originURI, zo)) {
					RemoteResource origin = of.createObject(originURI
							.toString(), RemoteResource.class);
					if (origin.load())
						return;
					origin = of.createObject(zo.toString(),
							RemoteResource.class);
					if (origin.load())
						return;
				}
			} catch (GatewayTimeout timeout) {
				gateway = timeout;
				blackList(addr, timeout);
			} catch (BadGateway bad) {
				gateway = bad;
				blackList(addr, bad);
			}
		}
		if (gateway != null)
			throw gateway;
		throw new NotFound("No PURL Server Available");
	}

	private String getZonedOrigin(String scheme, String auth) {
		int start = auth.indexOf('@');
		int end = auth.lastIndexOf(':');
		if (end < 0) {
			end = auth.length();
		}
		String hostname = auth.substring(start + 1, end);
		int idx = hostname.indexOf('.');
		if (idx > 0) {
			String zauth = auth.substring(0, start + 1)
					+ hostname.substring(idx + 1) + auth.substring(end);
			ParsedURI zo;
			zo = new ParsedURI(scheme, zauth, "/", null, null);
			return zo.toString();
		}
		return null;
	}

	private void consumeContent(HttpResponse resp) throws IOException {
		HttpEntity entity = resp.getEntity();
		if (entity != null) {
			entity.consumeContent();
		}
	}

	private String createSPARQL(String source) {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT REDUCED ?purl");
		sb.append("\nWHERE {");
		sb.append("\n\t{ ?purl a purl:PURL }");
		sb.append("\n\tUNION {?purl purl:domainOf ?origin }");
		sb.append("\n\tUNION {?purl a purl:RemoteResource }");
		sb.append("\n\tOPTIONAL {?purl ?z purl:Zoned FILTER(?z = rdf:type)}");
		sb.append("\nFILTER (?purl = <").append(source).append(">");
		ParsedURI uri = new ParsedURI(source);
		if (uri.isHierarchical()) {
			for (String match : pathFragments(uri, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
			String s = uri.getScheme();
			String a = uri.getAuthority();
			int at = a.indexOf('@');
			int idx = a.indexOf('.', at + 1);
			if (at > 0 && idx > 0) {
				a = a.substring(0, at + 1) + a.substring(idx + 1);
			} else if (idx > 0) {
				a = a.substring(idx + 1);
			}
			String path = uri.getPath();
			ParsedURI domain = new ParsedURI(s, a, path, null, null);
			String duri = domain.toString();
			sb.append("\n\t|| ?purl = <").append(duri).append(">");
			for (String match : pathFragments(domain, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
		}
		sb.append(")\n}\nORDER BY ?z desc(?purl)");
		return sb.toString();
	}

	private List<String> pathFragments(ParsedURI source, List<String> matches) {
		String s = source.getScheme();
		String a = source.getAuthority();
		String p = source.getPath();
		if (p.length() > 1 && p.startsWith("/")) {
			int idx = p.lastIndexOf('/', p.length() - 2);
			ParsedURI uri = new ParsedURI(s, a, p.substring(0, idx + 1), null,
					null);
			matches.add(uri.toString());
			return pathFragments(uri, matches);
		}
		return matches;
	}

}
