/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.concepts;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.openrdf.http.object.annotations.cacheControl;
import org.openrdf.http.object.annotations.operation;
import org.openrdf.http.object.annotations.parameter;
import org.openrdf.http.object.annotations.type;
import org.openrdf.http.object.traits.Realm;
import org.openrdf.repository.object.annotations.iri;
import org.openrdf.repository.object.annotations.matches;

@matches("/admin/realm")
public class AdminRealm implements Realm {

	@operation("allow-origin")
	@iri("http://www.openrdf.org/rdf/2009/httpobject#allow-origin")
	public String allowOrigin() {
		return null;
	}

	@cacheControl("no-store")
	@type("message/http")
	@operation("unauthorized")
	@iri("http://www.openrdf.org/rdf/2009/httpobject#unauthorized")
	public HttpResponse unauthorized() throws IOException {
		return null;
	}

	@operation("authorize")
	@iri("http://www.openrdf.org/rdf/2009/httpobject#authorized")
	public boolean authorize(@parameter("format") String format,
			@parameter("algorithm") String algorithm,
			@parameter("encoded") byte[] encoded,
			@parameter("addr") String addr, @parameter("method") String method) {
		return true;
	}

	@operation("authorize")
	@iri("http://www.openrdf.org/rdf/2009/httpobject#authorized")
	public boolean authorize(@parameter("format") String format,
			@parameter("algorithm") String algorithm,
			@parameter("encoded") byte[] encoded,
			@parameter("addr") String addr, @parameter("method") String method,
			Map<String, String[]> authorization) {
		return true;
	}

}
