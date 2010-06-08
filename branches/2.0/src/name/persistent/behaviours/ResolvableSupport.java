/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import info.aduna.net.ParsedURI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import name.persistent.concepts.MirroredDomain;
import name.persistent.concepts.Partial;
import name.persistent.concepts.Resolvable;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.exceptions.NotFound;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.RDFObject;

/**
 * Finds a PURL (possibly remote) for a request.
 * 
 * @author James Leigh
 */
public abstract class ResolvableSupport implements RDFObject, Resolvable {
	private static final String PREFIX = "PREFIX purl:<http://persistent.name/rdf/2010/purl#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

	public HttpResponse resolvePURL(String source, String qs, String accept,
			String lang, Set<String> via) throws Exception {
		return resolvePURL(source, qs, accept, lang, via, true);
	}

	public HttpResponse resolvePURL(String source, String qs, String accept,
			String lang, Set<String> via, boolean reload) throws Exception {
		ObjectConnection con = getObjectConnection();
		String sparql = createSPARQL(source);
		ObjectQuery query = con.prepareObjectQuery(sparql);
		List<Partial> result = query.evaluate(Partial.class).asList();
		if (result.isEmpty())
			throw new NotFound("Unknown PURL");
		Partial partial = result.get(0);
		if (reload && partial instanceof MirroredDomain) {
			if (((MirroredDomain) partial).reload()) {
				return resolvePURL(source, qs, accept, lang, via, false);
			}
		}
		return partial.resolvePURL(source, qs, accept, lang, via);
	}

	private String createSPARQL(String source) {
		ParsedURI uri = new ParsedURI(source);
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT REDUCED ?domain");
		sb.append("\nWHERE {{");
		sb.append("\n\t{ ?domain a purl:Domain }");
		sb.append("\n\tUNION {?domain a purl:MirroredDomain }");
		sb.append("\n\tUNION {?domain a purl:RemoteDomain }");
		sb.append("\n\tUNION {?domain a purl:Partial }");
		sb.append("\nFILTER (?domain = <").append(source).append(">");
		if (uri.isHierarchical()) {
			for (String match : pathFragments(uri, new ArrayList<String>())) {
				sb.append("\n\t|| ?domain = <").append(match).append(">");
			}
		}
		sb.append(")\n} UNION {");
		sb.append("\n\t{ ?domain ?z purl:ZonedDomain }");
		sb.append("\n\tUNION {?domain a purl:Partial;");
		sb.append(" purl:belongsTo [?z purl:ZonedDomain] }");
		sb.append("\nFILTER(?z = rdf:type)");
		sb.append("\nFILTER (");
		if (uri.isHierarchical()) {
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
			sb.append("?domain = <").append(duri).append(">");
			for (String match : pathFragments(domain, new ArrayList<String>())) {
				sb.append("\n\t|| ?domain = <").append(match).append(">");
			}
		}
		sb.append(")\n}}\nORDER BY ?z desc(?domain)");
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
