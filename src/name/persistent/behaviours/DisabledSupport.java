/*
 * Copyright (c) Zepheira LLC, Some rights reserved.
 * 
 * Source code developed for this project is licensed under the Apache
 * License, Version 2.0. See the file LICENSE.txt for details.
 */
package name.persistent.behaviours;

import java.util.Set;

import name.persistent.concepts.Disabled;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.openrdf.repository.object.annotations.precedes;

/**
 * Issues a 404 response for disabled PURLs.
 * 
 * @author James Leigh
 */
@precedes( { PURLSupport.class, ResolvableSupport.class, DomainSupport.class,
		MirroredDomainSupport.class })
public abstract class DisabledSupport extends PURLSupport implements Disabled {
	private static final ProtocolVersion HTTP11 = new ProtocolVersion("HTTP",
			1, 1);

	@Override
	public HttpResponse resolvePURL(String source, String qs, String accept,
			String language, Set<String> via) {
		BasicHttpResponse resp;
		resp = new BasicHttpResponse(HTTP11, 404, "Temporarily Gone");
		purlSetEntityHeaders(resp);
		return resp;
	}
}
