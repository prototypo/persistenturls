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

import name.persistent.concepts.PURL;
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
		List<PURL> result = query.evaluate(PURL.class).asList();
		if (result.isEmpty())
			throw new NotFound("Unknown PURL");
		PURL purl = result.get(0);
		return purl.resolvePURL(source, qs, accept, lang, via);
	}

	private String createSPARQL(String source) {
		ParsedURI uri = new ParsedURI(source);
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("SELECT REDUCED ?purl");
		sb.append("\nWHERE {{");
		sb.append("\n\t{ ?purl a purl:Domain }");
		sb.append("\n\tUNION {?purl a purl:MirroredDomain }");
		sb.append("\n\tUNION {?purl a purl:Partial }");
		sb.append("\nFILTER (?purl = <").append(source).append(">");
		if (uri.isHierarchical()) {
			for (String match : pathFragments(uri, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
		}
		sb.append(")\n} UNION {");
		sb.append("\n\t{ ?purl ?z purl:Domain }");
		sb.append("\n\tUNION {?purl ?z purl:MirroredDomain }");
		sb.append("\n\tUNION {?purl ?z purl:Partial }");
		sb.append("\n\tUNION {?purl ?z purl:PURL }");
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
			sb.append("?purl = <").append(duri).append(">");
			for (String match : pathFragments(domain, new ArrayList<String>())) {
				sb.append("\n\t|| ?purl = <").append(match).append(">");
			}
		}
		sb.append(")\n}}\nORDER BY ?z desc(?purl)");
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
